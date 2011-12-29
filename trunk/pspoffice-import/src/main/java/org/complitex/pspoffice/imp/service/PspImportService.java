/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.pspoffice.imp.service;

import org.complitex.dictionary.util.DateUtil;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import org.complitex.pspoffice.imp.entity.ImportStatus;
import org.complitex.dictionary.util.ImportStorageUtil;
import java.io.IOException;
import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Asynchronous;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import org.complitex.address.strategy.building.BuildingStrategy;
import org.complitex.address.strategy.building.entity.Building;
import org.complitex.address.strategy.building_address.BuildingAddressStrategy;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.service.LocaleBean;
import org.complitex.dictionary.service.exception.AbstractException;
import org.complitex.dictionary.service.exception.ImportCriticalException;
import org.complitex.dictionary.service.exception.ImportFileNotFoundException;
import org.complitex.dictionary.service.exception.ImportFileReadException;
import org.complitex.dictionary.util.ResourceUtil;
import org.complitex.pspoffice.document_type.strategy.DocumentTypeStrategy;
import org.complitex.pspoffice.imp.entity.BuildingCorrection;
import org.complitex.pspoffice.imp.entity.ImportMessage;
import org.complitex.pspoffice.imp.entity.ProcessItem;
import org.complitex.pspoffice.imp.entity.PspImportFile;
import org.complitex.pspoffice.imp.entity.ReferenceDataCorrection;
import org.complitex.pspoffice.imp.entity.StreetCorrection;
import org.complitex.pspoffice.imp.service.exception.OpenErrorDescriptionFileException;
import org.complitex.pspoffice.imp.service.exception.OpenErrorFileException;
import org.complitex.pspoffice.imp.service.exception.TooManyResultsException;
import org.complitex.pspoffice.ownerrelationship.strategy.OwnerRelationshipStrategy;
import org.complitex.pspoffice.ownership.strategy.OwnershipFormStrategy;
import org.complitex.pspoffice.registration_type.strategy.RegistrationTypeStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.complitex.pspoffice.imp.entity.ImportMessage.ImportMessageLevel.*;

/**
 *
 * @author Artem
 */
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
@TransactionManagement(TransactionManagementType.BEAN)
public class PspImportService {

    private static final Logger log = LoggerFactory.getLogger(PspImportService.class);
    private static final String RESOURCE_BUNDLE = PspImportService.class.getName();
    private static final char SEPARATOR = '\t';
    private static final String CHARSET = "UTF-8";
    private static final String ERROR_FILE_SUFFIX = "_errors.csv";
    private static final String ERROR_DESCRIPTION_FILE_SUFFIX = "_errors_description.txt";
    private static final int PROCESSING_BATCH = 100;
    @Resource
    private UserTransaction userTransaction;
    @EJB
    private StreetCorrectionBean streetCorrectionBean;
    @EJB
    private BuildingCorrectionBean buildingCorrectionBean;
    @EJB
    private BuildingStrategy buildingStrategy;
    @EJB
    private OwnershipFormStrategy ownershipFormStrategy;
    @EJB
    private OwnerRelationshipStrategy ownerRelationshipStrategy;
    @EJB
    private RegistrationTypeStrategy registrationTypeStrategy;
    @EJB
    private DocumentTypeStrategy documentTypeStrategy;
    @EJB
    private ReferenceDataCorrectionBean referenceDataCorrectionBean;
    @EJB
    private LocaleBean localeBean;
    private long SYSTEM_LOCALE_ID;
    private boolean processing;
    private Locale locale;
    private Long cityId;
    private Set<String> jekIds;
    private String importDirectory;
    private String errorsDirectory;
    private Map<PspImportFile, ImportStatus> loadingStatuses = new EnumMap<PspImportFile, ImportStatus>(PspImportFile.class);
    private Map<ProcessItem, ImportStatus> processingStatuses = new EnumMap<ProcessItem, ImportStatus>(ProcessItem.class);
    private Queue<ImportMessage> messages = new ConcurrentLinkedQueue<ImportMessage>();

    @PostConstruct
    private void init() {
        this.SYSTEM_LOCALE_ID = localeBean.getSystemLocaleObject().getId();
    }

    public boolean isProcessing() {
        return processing;
    }

    public ImportStatus getLoadingStatus(PspImportFile importFile) {
        return loadingStatuses.get(importFile);
    }

    public ImportStatus getProcessingStatus(ProcessItem processItem) {
        return processingStatuses.get(processItem);
    }

    public ImportMessage getNextMessage() {
        return messages.poll();
    }

    private void clean() {
        loadingStatuses.clear();
        processingStatuses.clear();
        messages.clear();
        cityId = null;
        locale = null;
        jekIds = null;
        importDirectory = null;
        errorsDirectory = null;
    }

    @Asynchronous
    public void startImport(long cityId, Set<String> jekIds, String importDirectiry, String errorsDirectory, Locale locale) {
        if (processing) {
            return;
        }

        clean();
        processing = true;

        this.cityId = cityId;
        this.jekIds = jekIds;
        this.importDirectory = importDirectiry;
        this.errorsDirectory = errorsDirectory;
        this.locale = locale;

        //load files
        try {
            userTransaction.begin();
            loadFiles();
            userTransaction.commit();
        } catch (Exception e) {
            processing = false;
            log.error("File loading error.", e);
            try {
                userTransaction.rollback();
            } catch (SystemException e1) {
                log.error("Couldn't to rollback transaction.", e1);
            }

            String errorMessage = e instanceof AbstractException ? e.getMessage() : new ImportCriticalException(e).getMessage();
            messages.add(new ImportMessage(errorMessage, ERROR));
        }

        if (!processing) {
            return;
        }

        //process files
        try {
            processFiles();
        } catch (Exception e) {
            log.error("Processing error.", e);
            String errorMessage = e instanceof AbstractException ? e.getMessage() : new ImportCriticalException(e).getMessage();
            messages.add(new ImportMessage(errorMessage, ERROR));
        } finally {
            processing = false;
        }
    }

    private void loadFiles() throws ImportFileReadException, ImportFileNotFoundException {
        loadStreets();
        loadBuildings();
        loadReferenceData(PspImportFile.OWNERSHIP_FORM, "ownership_form");
        loadReferenceData(PspImportFile.MILITARY_DUTY, "military_duty");
        loadReferenceData(PspImportFile.OWNER_RELATIONSHIP, "owner_relationship");
        loadReferenceData(PspImportFile.DEPARTURE_REASON, "departure_reason");
        loadReferenceData(PspImportFile.REGISTRATION_TYPE, "registration_type");
        loadReferenceData(PspImportFile.DOCUMENT_TYPE, "document_type");
        loadReferenceData(PspImportFile.OWNER_TYPE, "owner_type");
    }

    /**
     * id    Street type string(ukr)     Street name(ukr)     Street type string(rus)     Street name(rus)
     */
    private void loadStreets() throws ImportFileReadException, ImportFileNotFoundException {
        final PspImportFile file = PspImportFile.STREET;

        final Map<String, Boolean> loadFileStatusMap = Maps.newHashMap();
        for (String idjek : jekIds) {
            loadFileStatusMap.put(idjek, !streetCorrectionBean.exists(idjek));
        }

        final Set<String> loadJekIds = Sets.newHashSet();
        for (Map.Entry<String, Boolean> e : loadFileStatusMap.entrySet()) {
            String idjek = e.getKey();
            if (e.getValue()) {
                loadJekIds.add(idjek);
            } else {
                messages.add(new ImportMessage(getString("already_loaded_file", file.getFileName(), idjek), WARN));
            }
        }

        if (!loadJekIds.isEmpty()) {

            //start file importing:
            loadingStatuses.put(file, new ImportStatus(0));
            messages.add(new ImportMessage(getString("begin_loading_file", file.getFileName()), INFO));

            final CSVReader reader = getCsvReader(importDirectory, file, CHARSET, SEPARATOR);

            int recordIndex = 0;

            try {
                String[] line;

                while ((line = reader.readNext()) != null) {
                    recordIndex++;

                    long id = Long.parseLong(line[0].trim());

                    for (String idjek : loadJekIds) {
                        streetCorrectionBean.insert(new StreetCorrection(id, idjek, line[1].trim(), line[2].trim(),
                                line[3].trim(), line[4].trim(), getContent(line)));
                    }
                    loadingStatuses.get(file).increment();
                }

                //finish file importing
                messages.add(new ImportMessage(getString("finish_loading_file", file.getFileName()), INFO));
                loadingStatuses.get(file).finish();
            } catch (IOException e) {
                throw new ImportFileReadException(e, file.getFileName(), recordIndex);
            } catch (NumberFormatException e) {
                throw new ImportFileReadException(e, file.getFileName(), recordIndex);
            } finally {
                try {
                    reader.close();
                } catch (IOException e) {
                    log.error("Couldn't to close csv reader.", e);
                }
            }
        }
    }

    /**
     * id   idjek   idul    dom     korpus
     */
    private void loadBuildings() throws ImportFileReadException, ImportFileNotFoundException {
        final PspImportFile file = PspImportFile.BUILDING;

        final Map<String, Boolean> loadFileStatusMap = Maps.newHashMap();
        for (String idjek : jekIds) {
            loadFileStatusMap.put(idjek, !buildingCorrectionBean.exists(idjek));
        }

        final Set<String> loadJekIds = Sets.newHashSet();
        for (Map.Entry<String, Boolean> e : loadFileStatusMap.entrySet()) {
            String idjek = e.getKey();
            if (e.getValue()) {
                loadJekIds.add(idjek);
            } else {
                messages.add(new ImportMessage(getString("already_loaded_file", file.getFileName(), idjek), WARN));
            }
        }

        if (!loadJekIds.isEmpty()) {

            //start file importing:
            loadingStatuses.put(file, new ImportStatus(0));
            messages.add(new ImportMessage(getString("begin_loading_file", file.getFileName()), INFO));

            final CSVReader reader = getCsvReader(importDirectory, file, CHARSET, SEPARATOR);

            int recordIndex = 0;

            try {
                String[] line;

                while ((line = reader.readNext()) != null) {
                    recordIndex++;

                    long id = Long.parseLong(line[0].trim());

                    String idjek = line[1].trim();

                    if (loadJekIds.contains(idjek)) {
                        buildingCorrectionBean.insert(new BuildingCorrection(id, idjek, line[2].trim(),
                                line[3].trim(), line[4].trim(), getContent(line)));
                        loadingStatuses.get(file).increment();
                    }
                }

                //finish file importing
                messages.add(new ImportMessage(getString("finish_loading_file", file.getFileName()), INFO));
                loadingStatuses.get(file).finish();
            } catch (IOException e) {
                throw new ImportFileReadException(e, file.getFileName(), recordIndex);
            } catch (NumberFormatException e) {
                throw new ImportFileReadException(e, file.getFileName(), recordIndex);
            } finally {
                try {
                    reader.close();
                } catch (IOException e) {
                    log.error("Couldn't to close csv reader.", e);
                }
            }
        }
    }

    /**
     * id    nkod
     */
    private void loadReferenceData(final PspImportFile file, final String entity) throws ImportFileReadException, ImportFileNotFoundException {
        final Map<String, Boolean> loadFileStatusMap = Maps.newHashMap();
        for (String idjek : jekIds) {
            loadFileStatusMap.put(idjek, !referenceDataCorrectionBean.exists(entity, idjek));
        }

        final Set<String> loadJekIds = Sets.newHashSet();
        for (Map.Entry<String, Boolean> e : loadFileStatusMap.entrySet()) {
            String idjek = e.getKey();
            if (e.getValue()) {
                loadJekIds.add(idjek);
            } else {
                messages.add(new ImportMessage(getString("already_loaded_file", file.getFileName(), idjek), WARN));
            }
        }

        if (!loadJekIds.isEmpty()) {

            //start file importing:
            loadingStatuses.put(file, new ImportStatus(0));
            messages.add(new ImportMessage(getString("begin_loading_file", file.getFileName()), INFO));

            final CSVReader reader = getCsvReader(importDirectory, file, CHARSET, SEPARATOR);

            int recordIndex = 0;

            try {
                String[] line;

                while ((line = reader.readNext()) != null) {
                    recordIndex++;

                    long id = Long.parseLong(line[0].trim());

                    for (String idjek : loadJekIds) {
                        referenceDataCorrectionBean.insert(new ReferenceDataCorrection(entity, id, idjek, line[1].trim(),
                                getContent(line)));
                    }
                    loadingStatuses.get(file).increment();
                }

                //finish file importing
                messages.add(new ImportMessage(getString("finish_loading_file", file.getFileName()), INFO));
                loadingStatuses.get(file).finish();
            } catch (IOException e) {
                throw new ImportFileReadException(e, file.getFileName(), recordIndex);
            } catch (NumberFormatException e) {
                throw new ImportFileReadException(e, file.getFileName(), recordIndex);
            } finally {
                try {
                    reader.close();
                } catch (IOException e) {
                    log.error("Couldn't to close csv reader.", e);
                }
            }
        }
    }

    private void processFiles() throws OpenErrorFileException, OpenErrorDescriptionFileException {
        processStreetsAndBuildings();
        processOwnershipForms();
        processOwnerRelationships();
        processRegistrationsTypes();
        processDocumentsTypes();
        processOwnerTypes();
    }

    private void processStreetsAndBuildings() throws OpenErrorFileException, OpenErrorDescriptionFileException {
        try {
            final ProcessItem item = ProcessItem.STREET_BUILDING;

            BufferedWriter streetErrorFile = null;
            BufferedWriter streetErrorDescriptionFile = null;
            BufferedWriter buildingErrorFile = null;
            BufferedWriter buildingErrorDescriptionFile = null;

            processingStatuses.put(item, new ImportStatus(0));
            final int count = buildingCorrectionBean.countForProcessing(jekIds);
            messages.add(new ImportMessage(getString("begin_street_building_processing", count), INFO));
            boolean wasErrors = false;

            try {
                for (String idjek : jekIds) {
                    int jekCount = buildingCorrectionBean.countForProcessing(idjek);
                    while (jekCount > 0) {
                        List<BuildingCorrection> buildings = buildingCorrectionBean.findForProcessing(idjek, PROCESSING_BATCH);
                        for (BuildingCorrection building : buildings) {
                            if (building.getSystemBuildingId() == null) {
                                userTransaction.begin();

                                String buildingErrorDescription = null;
                                String streetErrorDescription = null;

                                String idul = building.getIdul();
                                final StreetCorrection street = streetCorrectionBean.findById(idul, idjek);
                                if (street == null) {
                                    buildingErrorDescription = getString("buiding_invalid_street_id",
                                            building.getId(), building.getIdjek(), idul);
                                } else {
                                    Long systemStreetId = street.getSystemStreetId();
                                    if (systemStreetId == null && !street.isProcessed()) {
                                        final String ukrStreet = street.getNkod();
                                        final String rusStreet = street.getNkod1();
                                        final String ukrStreetType = street.getUtype();
                                        final String rusStreetType = street.getRtype();

                                        //street type
                                        Long systemStreetTypeId = streetCorrectionBean.findSystemStreetType(ukrStreetType, rusStreetType);
                                        if (systemStreetTypeId == null) {
                                            streetErrorDescription = getString("street_type_not_resolved", street.getId(),
                                                    street.getIdjek(), ukrStreetType, rusStreetType);
                                        } else {
                                            //street
                                            systemStreetId = streetCorrectionBean.findSystemStreet(cityId,
                                                    systemStreetTypeId, ukrStreet, rusStreet);
                                            if (systemStreetId == null) {
                                                streetErrorDescription = getString("street_not_resolved", street.getId(),
                                                        street.getIdjek(), ukrStreet, rusStreet);
                                            } else {
                                                street.setSystemStreetId(systemStreetId);
                                            }
                                        }
                                        street.setProcessed(true);
                                        streetCorrectionBean.update(street);
                                    }
                                    if (systemStreetId != null) {
                                        final String dom = building.getDom();
                                        final String korpus = building.getKorpus();

                                        try {
                                            Long systemBuildingId =
                                                    buildingCorrectionBean.findSystemBuilding(systemStreetId, dom, korpus);
                                            if (systemBuildingId == null) {
                                                Building systemBuilding = buildingStrategy.newInstance();
                                                DomainObject systemBuildingAddress = systemBuilding.getPrimaryAddress();

                                                systemBuildingAddress.setParentEntityId(
                                                        BuildingAddressStrategy.PARENT_STREET_ENTITY_ID);
                                                systemBuildingAddress.setParentId(systemStreetId);

                                                AttributeUtil.setValue(systemBuildingAddress.getAttribute(
                                                        BuildingAddressStrategy.NUMBER), SYSTEM_LOCALE_ID, dom);
                                                AttributeUtil.setValue(systemBuildingAddress.getAttribute(
                                                        BuildingAddressStrategy.CORP), SYSTEM_LOCALE_ID, korpus);

                                                buildingStrategy.insert(systemBuilding, DateUtil.getCurrentDate());
                                                systemBuildingId = systemBuilding.getId();
                                            }
                                            building.setSystemBuildingId(systemBuildingId);
                                        } catch (TooManyResultsException e) {
                                            buildingErrorDescription = getString("building_system_many_objects",
                                                    building.getId(), building.getIdjek(), building.getDom(), building.getKorpus(),
                                                    building.getIdul());
                                        }
                                    } else {
                                        buildingErrorDescription = getString("building_system_street_not_resolved",
                                                building.getId(), building.getIdjek(), idul);
                                    }
                                }

                                building.setProcessed(true);
                                buildingCorrectionBean.update(building);
                                userTransaction.commit();

                                processingStatuses.get(item).increment();

                                if (streetErrorDescription != null) {
                                    wasErrors = true;
                                    if (streetErrorFile == null) {
                                        streetErrorFile = getErrorFile(errorsDirectory, PspImportFile.STREET);
                                        streetErrorFile.write(PspImportFile.STREET.getCsvHeader());
                                        streetErrorFile.newLine();
                                    }
                                    if (streetErrorDescriptionFile == null) {
                                        streetErrorDescriptionFile = getErrorDescriptionFile(errorsDirectory,
                                                PspImportFile.STREET);
                                    }

                                    streetErrorFile.write(street.getContent());
                                    streetErrorFile.newLine();

                                    streetErrorDescriptionFile.write(streetErrorDescription);
                                    streetErrorDescriptionFile.newLine();
                                }

                                if (buildingErrorDescription != null) {
                                    wasErrors = true;
                                    if (buildingErrorFile == null) {
                                        buildingErrorFile = getErrorFile(errorsDirectory, PspImportFile.BUILDING);
                                        buildingErrorFile.write(PspImportFile.BUILDING.getCsvHeader());
                                        buildingErrorFile.newLine();
                                    }
                                    if (buildingErrorDescriptionFile == null) {
                                        buildingErrorDescriptionFile = getErrorDescriptionFile(errorsDirectory,
                                                PspImportFile.BUILDING);
                                    }

                                    buildingErrorFile.write(building.getContent());
                                    buildingErrorFile.newLine();

                                    buildingErrorDescriptionFile.write(buildingErrorDescription);
                                    buildingErrorDescriptionFile.newLine();
                                }
                            }
                        }
                        jekCount = buildingCorrectionBean.countForProcessing(idjek);
                    }
                }
            } catch (Exception e) {
                try {
                    if (userTransaction.getStatus() != Status.STATUS_NO_TRANSACTION) {
                        userTransaction.rollback();
                    }
                } catch (Exception e1) {
                    log.error("Couldn't to rollback transaction.", e1);
                }

                throw new RuntimeException(e);
            } finally {
                if (streetErrorFile != null) {
                    try {
                        streetErrorFile.close();
                    } catch (IOException e) {
                        log.error("Couldn't to close file stream.", e);
                    }
                }
                if (streetErrorDescriptionFile != null) {
                    try {
                        streetErrorDescriptionFile.close();
                    } catch (IOException e) {
                        log.error("Couldn't to close file stream.", e);
                    }
                }
                if (buildingErrorFile != null) {
                    try {
                        buildingErrorFile.close();
                    } catch (IOException e) {
                        log.error("Couldn't to close file stream.", e);
                    }
                }
                if (buildingErrorDescriptionFile != null) {
                    try {
                        buildingErrorDescriptionFile.close();
                    } catch (IOException e) {
                        log.error("Couldn't to close file stream.", e);
                    }
                }
            }

            if (wasErrors) {
                messages.add(new ImportMessage(getString("fail_finish_street_building_processing", count), WARN));
            } else {
                messages.add(new ImportMessage(getString("success_finish_street_building_processing", count), INFO));
            }
            processingStatuses.get(item).finish();
        } catch (RuntimeException e) {
            throw e;
        } finally {
            try {
                userTransaction.begin();

                streetCorrectionBean.clearProcessingStatus(jekIds);
                buildingCorrectionBean.clearProcessingStatus(jekIds);

                userTransaction.commit();
            } catch (Exception e) {
                try {
                    userTransaction.rollback();
                } catch (SystemException e1) {
                    log.error("Couldn't to rollback transaction.", e1);
                }
                log.error("Couldn't to clear processing status for streets and buildings.", e);
            }
        }
    }

    private void processOwnershipForms() throws OpenErrorFileException, OpenErrorDescriptionFileException {
        final String entity = "ownership_form";
        try {
            final ProcessItem item = ProcessItem.OWNERSHIP_FORM;

            BufferedWriter ownershipFormErrorFile = null;
            BufferedWriter ownershipFormErrorDescriptionFile = null;

            processingStatuses.put(item, new ImportStatus(0));
            final int count = referenceDataCorrectionBean.countForProcessing(entity, jekIds);
            messages.add(new ImportMessage(getString("begin_ownership_form_processing", count), INFO));
            boolean wasErrors = false;

            try {
                for (String idjek : jekIds) {
                    int jekCount = referenceDataCorrectionBean.countForProcessing(entity, idjek);
                    while (jekCount > 0) {
                        List<ReferenceDataCorrection> ownershipForms =
                                referenceDataCorrectionBean.findForProcessing(entity, idjek, PROCESSING_BATCH);

                        userTransaction.begin();
                        for (ReferenceDataCorrection ownershipForm : ownershipForms) {
                            String errorDescription = null;
                            try {
                                Long systemOwnershipFormId = referenceDataCorrectionBean.findSystemObject(entity, ownershipForm.getNkod());
                                if (systemOwnershipFormId == null) {
                                    DomainObject systemOwnershipForm = ownershipFormStrategy.newInstance();
                                    AttributeUtil.setValue(systemOwnershipForm.getAttribute(OwnershipFormStrategy.NAME),
                                            ownershipForm.getNkod());
                                    ownershipFormStrategy.insert(systemOwnershipForm, DateUtil.getCurrentDate());
                                    systemOwnershipFormId = systemOwnershipForm.getId();
                                }
                                ownershipForm.setSystemObjectId(systemOwnershipFormId);
                            } catch (TooManyResultsException e) {
                                errorDescription = getString("ownership_form_system_many_objects", ownershipForm.getId(),
                                        idjek, ownershipForm.getNkod());
                            }

                            ownershipForm.setProcessed(true);
                            referenceDataCorrectionBean.update(ownershipForm);

                            processingStatuses.get(item).increment();

                            if (errorDescription != null) {
                                wasErrors = true;
                                if (ownershipFormErrorFile == null) {
                                    ownershipFormErrorFile = getErrorFile(errorsDirectory, PspImportFile.OWNERSHIP_FORM);
                                    ownershipFormErrorFile.write(PspImportFile.OWNERSHIP_FORM.getCsvHeader());
                                    ownershipFormErrorFile.newLine();
                                }
                                if (ownershipFormErrorDescriptionFile == null) {
                                    ownershipFormErrorDescriptionFile = getErrorDescriptionFile(errorsDirectory,
                                            PspImportFile.OWNERSHIP_FORM);
                                }

                                ownershipFormErrorFile.write(ownershipForm.getContent());
                                ownershipFormErrorFile.newLine();

                                ownershipFormErrorDescriptionFile.write(errorDescription);
                                ownershipFormErrorDescriptionFile.newLine();
                            }
                        }

                        userTransaction.commit();
                        jekCount = referenceDataCorrectionBean.countForProcessing(entity, idjek);
                    }
                }
            } catch (Exception e) {
                try {
                    if (userTransaction.getStatus() != Status.STATUS_NO_TRANSACTION) {
                        userTransaction.rollback();
                    }
                } catch (Exception e1) {
                    log.error("Couldn't to rollback transaction.", e1);
                }

                throw new RuntimeException(e);
            } finally {
                if (ownershipFormErrorFile != null) {
                    try {
                        ownershipFormErrorFile.close();
                    } catch (IOException e) {
                        log.error("Couldn't to close file stream.", e);
                    }
                }
                if (ownershipFormErrorDescriptionFile != null) {
                    try {
                        ownershipFormErrorDescriptionFile.close();
                    } catch (IOException e) {
                        log.error("Couldn't to close file stream.", e);
                    }
                }
            }

            if (wasErrors) {
                messages.add(new ImportMessage(getString("fail_finish_ownership_form_processing", count), WARN));
            } else {
                messages.add(new ImportMessage(getString("success_finish_ownership_form_processing", count), INFO));
            }
            processingStatuses.get(item).finish();
        } catch (RuntimeException e) {
            throw e;
        } finally {
            try {
                userTransaction.begin();

                referenceDataCorrectionBean.clearProcessingStatus(entity, jekIds);

                userTransaction.commit();
            } catch (Exception e) {
                try {
                    userTransaction.rollback();
                } catch (SystemException e1) {
                    log.error("Couldn't to rollback transaction.", e1);
                }
                log.error("Couldn't to clear processing status for ownership forms.", e);
            }
        }
    }

    private void processOwnerRelationships() throws OpenErrorFileException, OpenErrorDescriptionFileException {
        final String entity = "owner_relationship";
        try {
            final ProcessItem item = ProcessItem.OWNER_RELATIONSHIP;

            BufferedWriter ownerRelationshipErrorFile = null;
            BufferedWriter ownerRelationshipErrorDescriptionFile = null;

            processingStatuses.put(item, new ImportStatus(0));
            final int count = referenceDataCorrectionBean.countForProcessing(entity, jekIds);
            messages.add(new ImportMessage(getString("begin_owner_relationship_processing", count), INFO));
            boolean wasErrors = false;

            try {
                for (String idjek : jekIds) {
                    int jekCount = referenceDataCorrectionBean.countForProcessing(entity, idjek);
                    while (jekCount > 0) {
                        List<ReferenceDataCorrection> ownerRelationships =
                                referenceDataCorrectionBean.findForProcessing(entity, idjek, PROCESSING_BATCH);

                        userTransaction.begin();
                        for (ReferenceDataCorrection ownerRelationship : ownerRelationships) {
                            String errorDescription = null;

                            if (ownerRelationship.getId() == ReferenceDataCorrectionBean.OWNER) { //владелец квартиры
                                ownerRelationship.setSystemObjectId(OwnerRelationshipStrategy.OWNER);
                            } else if (ownerRelationship.getId() == ReferenceDataCorrectionBean.DAUGHTER) { // дочь
                                ownerRelationship.setSystemObjectId(OwnerRelationshipStrategy.DAUGHTER);
                            } else if (ownerRelationship.getId() == ReferenceDataCorrectionBean.SON) { // сын
                                ownerRelationship.setSystemObjectId(OwnerRelationshipStrategy.SON);
                            } else {
                                try {
                                    Long systemOwnerRelationshipId =
                                            referenceDataCorrectionBean.findSystemObject(entity, ownerRelationship.getNkod());
                                    if (systemOwnerRelationshipId == null) {
                                        DomainObject systemOwnerRelationship = ownerRelationshipStrategy.newInstance();
                                        AttributeUtil.setValue(systemOwnerRelationship.getAttribute(OwnerRelationshipStrategy.NAME),
                                                ownerRelationship.getNkod());
                                        ownerRelationshipStrategy.insert(systemOwnerRelationship, DateUtil.getCurrentDate());
                                        systemOwnerRelationshipId = systemOwnerRelationship.getId();
                                    }
                                    ownerRelationship.setSystemObjectId(systemOwnerRelationshipId);
                                } catch (TooManyResultsException e) {
                                    errorDescription = getString("owner_relationship_system_many_objects", ownerRelationship.getId(),
                                            idjek, ownerRelationship.getNkod());
                                }
                            }

                            ownerRelationship.setProcessed(true);
                            referenceDataCorrectionBean.update(ownerRelationship);

                            processingStatuses.get(item).increment();

                            if (errorDescription != null) {
                                wasErrors = true;
                                if (ownerRelationshipErrorFile == null) {
                                    ownerRelationshipErrorFile = getErrorFile(errorsDirectory, PspImportFile.OWNER_RELATIONSHIP);
                                    ownerRelationshipErrorFile.write(PspImportFile.OWNER_RELATIONSHIP.getCsvHeader());
                                    ownerRelationshipErrorFile.newLine();
                                }
                                if (ownerRelationshipErrorDescriptionFile == null) {
                                    ownerRelationshipErrorDescriptionFile = getErrorDescriptionFile(errorsDirectory,
                                            PspImportFile.OWNER_RELATIONSHIP);
                                }

                                ownerRelationshipErrorFile.write(ownerRelationship.getContent());
                                ownerRelationshipErrorFile.newLine();

                                ownerRelationshipErrorDescriptionFile.write(errorDescription);
                                ownerRelationshipErrorDescriptionFile.newLine();
                            }
                        }

                        userTransaction.commit();
                        jekCount = referenceDataCorrectionBean.countForProcessing(entity, idjek);
                    }
                }

                try {
                    referenceDataCorrectionBean.checkReservedOwnerRelationships();
                } catch (ReferenceDataCorrectionBean.OwnerRelationshipsNotResolved e) {
                    wasErrors = true;

                    StringBuilder sb = new StringBuilder();
                    if (!e.isOwnerResolved()) {
                        sb.append(ownerRelationshipStrategy.displayDomainObject(
                                ownerRelationshipStrategy.findById(OwnerRelationshipStrategy.OWNER, true), localeBean.getSystemLocale())).
                                append(", ");
                    }
                    if (!e.isDaughterResolved()) {
                        sb.append(ownerRelationshipStrategy.displayDomainObject(
                                ownerRelationshipStrategy.findById(OwnerRelationshipStrategy.DAUGHTER, true), localeBean.getSystemLocale())).
                                append(", ");
                    }
                    if (!e.isSonResolved()) {
                        sb.append(ownerRelationshipStrategy.displayDomainObject(
                                ownerRelationshipStrategy.findById(OwnerRelationshipStrategy.SON, true), localeBean.getSystemLocale())).
                                append(", ");
                    }
                    sb.delete(sb.length() - 2, sb.length());
                    String error = getString("reserved_owner_relationship_not_resolved", sb.toString());
                    if (ownerRelationshipErrorDescriptionFile == null) {
                        ownerRelationshipErrorDescriptionFile = getErrorDescriptionFile(errorsDirectory,
                                PspImportFile.OWNER_RELATIONSHIP);
                    }
                    ownerRelationshipErrorDescriptionFile.write(error);
                    ownerRelationshipErrorDescriptionFile.newLine();

                    messages.add(new ImportMessage(error, WARN));
                }
            } catch (Exception e) {
                try {
                    if (userTransaction.getStatus() != Status.STATUS_NO_TRANSACTION) {
                        userTransaction.rollback();
                    }
                } catch (Exception e1) {
                    log.error("Couldn't to rollback transaction.", e1);
                }

                throw new RuntimeException(e);
            } finally {
                if (ownerRelationshipErrorFile != null) {
                    try {
                        ownerRelationshipErrorFile.close();
                    } catch (IOException e) {
                        log.error("Couldn't to close file stream.", e);
                    }
                }
                if (ownerRelationshipErrorDescriptionFile != null) {
                    try {
                        ownerRelationshipErrorDescriptionFile.close();
                    } catch (IOException e) {
                        log.error("Couldn't to close file stream.", e);
                    }
                }
            }

            if (wasErrors) {
                messages.add(new ImportMessage(getString("fail_finish_owner_relationship_processing", count), WARN));
            } else {
                messages.add(new ImportMessage(getString("success_finish_owner_relationship_processing", count), INFO));
            }
            processingStatuses.get(item).finish();
        } catch (RuntimeException e) {
            throw e;
        } finally {
            try {
                userTransaction.begin();

                referenceDataCorrectionBean.clearProcessingStatus(entity, jekIds);

                userTransaction.commit();
            } catch (Exception e) {
                try {
                    userTransaction.rollback();
                } catch (SystemException e1) {
                    log.error("Couldn't to rollback transaction.", e1);
                }
                log.error("Couldn't to clear processing status for owner relationships.", e);
            }
        }
    }

    private void processRegistrationsTypes() throws OpenErrorFileException, OpenErrorDescriptionFileException {
        final String entity = "registration_type";
        try {
            final ProcessItem item = ProcessItem.REGISTRATION_TYPE;

            BufferedWriter registrationTypeErrorFile = null;
            BufferedWriter registrationTypeErrorDescriptionFile = null;

            processingStatuses.put(item, new ImportStatus(0));
            final int count = referenceDataCorrectionBean.countForProcessing(entity, jekIds);
            messages.add(new ImportMessage(getString("begin_registration_type_processing", count), INFO));
            boolean wasErrors = false;

            try {
                for (String idjek : jekIds) {
                    int jekCount = referenceDataCorrectionBean.countForProcessing(entity, idjek);
                    while (jekCount > 0) {
                        List<ReferenceDataCorrection> registrationTypes =
                                referenceDataCorrectionBean.findForProcessing(entity, idjek, PROCESSING_BATCH);

                        userTransaction.begin();
                        for (ReferenceDataCorrection registrationType : registrationTypes) {
                            String errorDescription = null;

                            if (registrationType.getId() == ReferenceDataCorrectionBean.PERMANENT) { //постоянная регистрация
                                registrationType.setSystemObjectId(RegistrationTypeStrategy.PERMANENT);
                            } else if (registrationType.getId() == ReferenceDataCorrectionBean.TEMPORAL) { //временная регистрация
                                registrationType.setSystemObjectId(RegistrationTypeStrategy.TEMPORAL);
                            } else {
                                try {
                                    Long systemRegistrationTypeId =
                                            referenceDataCorrectionBean.findSystemObject(entity, registrationType.getNkod());
                                    if (systemRegistrationTypeId == null) {
                                        DomainObject systemRegistrationType = registrationTypeStrategy.newInstance();
                                        AttributeUtil.setValue(systemRegistrationType.getAttribute(RegistrationTypeStrategy.NAME),
                                                registrationType.getNkod());
                                        registrationTypeStrategy.insert(systemRegistrationType, DateUtil.getCurrentDate());
                                        systemRegistrationTypeId = systemRegistrationType.getId();
                                    }
                                    registrationType.setSystemObjectId(systemRegistrationTypeId);
                                } catch (TooManyResultsException e) {
                                    errorDescription = getString("registration_type_system_many_objects", registrationType.getId(),
                                            idjek, registrationType.getNkod());
                                }
                            }

                            registrationType.setProcessed(true);
                            referenceDataCorrectionBean.update(registrationType);

                            processingStatuses.get(item).increment();

                            if (errorDescription != null) {
                                wasErrors = true;
                                if (registrationTypeErrorFile == null) {
                                    registrationTypeErrorFile = getErrorFile(errorsDirectory, PspImportFile.REGISTRATION_TYPE);
                                    registrationTypeErrorFile.write(PspImportFile.REGISTRATION_TYPE.getCsvHeader());
                                    registrationTypeErrorFile.newLine();
                                }
                                if (registrationTypeErrorDescriptionFile == null) {
                                    registrationTypeErrorDescriptionFile = getErrorDescriptionFile(errorsDirectory,
                                            PspImportFile.REGISTRATION_TYPE);
                                }

                                registrationTypeErrorFile.write(registrationType.getContent());
                                registrationTypeErrorFile.newLine();

                                registrationTypeErrorDescriptionFile.write(errorDescription);
                                registrationTypeErrorDescriptionFile.newLine();
                            }
                        }

                        userTransaction.commit();
                        jekCount = referenceDataCorrectionBean.countForProcessing(entity, idjek);
                    }
                }

                try {
                    referenceDataCorrectionBean.checkReservedRegistrationTypes();
                } catch (ReferenceDataCorrectionBean.RegistrationTypesNotResolved e) {
                    wasErrors = true;

                    StringBuilder sb = new StringBuilder();
                    if (!e.isPermanentResolved()) {
                        sb.append(registrationTypeStrategy.displayDomainObject(
                                registrationTypeStrategy.findById(RegistrationTypeStrategy.PERMANENT, true),
                                localeBean.getSystemLocale())).
                                append(", ");
                    }
                    if (!e.isTemporalResolved()) {
                        sb.append(registrationTypeStrategy.displayDomainObject(
                                registrationTypeStrategy.findById(RegistrationTypeStrategy.TEMPORAL, true),
                                localeBean.getSystemLocale())).
                                append(", ");
                    }
                    sb.delete(sb.length() - 2, sb.length());
                    String error = getString("reserved_registration_type_not_resolved", sb.toString());
                    if (registrationTypeErrorDescriptionFile == null) {
                        registrationTypeErrorDescriptionFile = getErrorDescriptionFile(errorsDirectory,
                                PspImportFile.REGISTRATION_TYPE);
                    }
                    registrationTypeErrorDescriptionFile.write(error);
                    registrationTypeErrorDescriptionFile.newLine();

                    messages.add(new ImportMessage(error, WARN));
                }
            } catch (Exception e) {
                try {
                    if (userTransaction.getStatus() != Status.STATUS_NO_TRANSACTION) {
                        userTransaction.rollback();
                    }
                } catch (Exception e1) {
                    log.error("Couldn't to rollback transaction.", e1);
                }

                throw new RuntimeException(e);
            } finally {
                if (registrationTypeErrorFile != null) {
                    try {
                        registrationTypeErrorFile.close();
                    } catch (IOException e) {
                        log.error("Couldn't to close file stream.", e);
                    }
                }
                if (registrationTypeErrorDescriptionFile != null) {
                    try {
                        registrationTypeErrorDescriptionFile.close();
                    } catch (IOException e) {
                        log.error("Couldn't to close file stream.", e);
                    }
                }
            }

            if (wasErrors) {
                messages.add(new ImportMessage(getString("fail_finish_registration_type_processing", count), WARN));
            } else {
                messages.add(new ImportMessage(getString("success_finish_registration_type_processing", count), INFO));
            }
            processingStatuses.get(item).finish();
        } catch (RuntimeException e) {
            throw e;
        } finally {
            try {
                userTransaction.begin();

                referenceDataCorrectionBean.clearProcessingStatus(entity, jekIds);

                userTransaction.commit();
            } catch (Exception e) {
                try {
                    userTransaction.rollback();
                } catch (SystemException e1) {
                    log.error("Couldn't to rollback transaction.", e1);
                }
                log.error("Couldn't to clear processing status for registration types.", e);
            }
        }
    }

    private void processDocumentsTypes() throws OpenErrorFileException, OpenErrorDescriptionFileException {
        final ProcessItem item = ProcessItem.DOCUMENT_TYPE;
        BufferedWriter documentTypeErrorDescriptionFile = null;

        try {
            userTransaction.begin();
            referenceDataCorrectionBean.putReservedDocumentTypes();
            userTransaction.commit();

            try {
                referenceDataCorrectionBean.checkReservedDocumentTypes();
                messages.add(new ImportMessage(getString("success_finish_document_type_processing"), INFO));
            } catch (ReferenceDataCorrectionBean.DocumentTypesNotResolved e) {
                StringBuilder sb = new StringBuilder();
                if (!e.isPassportResolved()) {
                    sb.append(documentTypeStrategy.displayDomainObject(
                            documentTypeStrategy.findById(DocumentTypeStrategy.PASSPORT, true),
                            localeBean.getSystemLocale())).
                            append(", ");
                }
                if (!e.isBirthCertificateResolved()) {
                    sb.append(documentTypeStrategy.displayDomainObject(
                            documentTypeStrategy.findById(DocumentTypeStrategy.BIRTH_CERTIFICATE, true),
                            localeBean.getSystemLocale())).
                            append(", ");
                }
                sb.delete(sb.length() - 2, sb.length());
                String error = getString("fail_finish_document_type_processing", sb.toString());

                documentTypeErrorDescriptionFile = getErrorDescriptionFile(errorsDirectory, PspImportFile.DOCUMENT_TYPE);
                documentTypeErrorDescriptionFile.write(error);
                documentTypeErrorDescriptionFile.newLine();

                messages.add(new ImportMessage(error, WARN));
            }
            ImportStatus status = new ImportStatus(0);
            status.finish();
            processingStatuses.put(item, status);
        } catch (Exception e) {
            try {
                if (userTransaction.getStatus() != Status.STATUS_NO_TRANSACTION) {
                    userTransaction.rollback();
                }
            } catch (Exception e1) {
                log.error("Couldn't to rollback transaction.", e1);
            }
            throw new RuntimeException(e);
        } finally {
            if (documentTypeErrorDescriptionFile != null) {
                try {
                    documentTypeErrorDescriptionFile.close();
                } catch (IOException e) {
                    log.error("Couldn't to close file stream.", e);
                }
            }
        }
    }

    private void processOwnerTypes() throws OpenErrorFileException, OpenErrorDescriptionFileException {
        final ProcessItem item = ProcessItem.OWNER_TYPE;
        BufferedWriter ownerTypeErrorDescriptionFile = null;

        try {
            if (referenceDataCorrectionBean.checkReservedOwnerType()) {
                messages.add(new ImportMessage(getString("success_finish_owner_type_processing"), INFO));
            } else {
                String error = getString("fail_finish_owner_type_processing");

                ownerTypeErrorDescriptionFile = getErrorDescriptionFile(errorsDirectory, PspImportFile.OWNER_TYPE);
                ownerTypeErrorDescriptionFile.write(error);
                ownerTypeErrorDescriptionFile.newLine();

                messages.add(new ImportMessage(error, WARN));
            }

            ImportStatus status = new ImportStatus(0);
            status.finish();
            processingStatuses.put(item, status);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (ownerTypeErrorDescriptionFile != null) {
                try {
                    ownerTypeErrorDescriptionFile.close();
                } catch (IOException e) {
                    log.error("Couldn't to close file stream.", e);
                }
            }
        }
    }

    private String getString(String key, Object... parameters) {
        return ResourceUtil.getFormatString(RESOURCE_BUNDLE, key, locale, parameters);
    }

    private static String getContent(String[] line) {
        StringBuilder content = new StringBuilder();
        boolean firstLine = true;
        for (String field : line) {
            if (!firstLine) {
                content.append(SEPARATOR);
            } else {
                firstLine = false;
            }
            content.append(field);
        }
        return content.toString();
    }

    public void cleanData(Set<String> jekIds) {
        try {
            clean();

            userTransaction.begin();

            streetCorrectionBean.cleanData(jekIds);
            buildingCorrectionBean.cleanData(jekIds);
            referenceDataCorrectionBean.cleanData("ownership_form", jekIds);
            referenceDataCorrectionBean.cleanData("military_duty", jekIds);
            referenceDataCorrectionBean.cleanData("owner_relationship", jekIds);
            referenceDataCorrectionBean.cleanData("departure_reason", jekIds);
            referenceDataCorrectionBean.cleanData("registration_type", jekIds);
            referenceDataCorrectionBean.cleanData("document_type", jekIds);
            referenceDataCorrectionBean.cleanData("owner_type", jekIds);

            userTransaction.commit();
        } catch (Exception e) {
            try {
                userTransaction.rollback();
            } catch (SystemException e1) {
                log.error("Couldn't to rollback transaction.", e1);
            }
            throw new RuntimeException(e);
        }
    }

    private int getRecordCount(String dir, PspImportFile file) throws ImportFileNotFoundException, ImportFileReadException {
        return ImportStorageUtil.getRecordCount(dir, file);
    }

    private CSVReader getCsvReader(String dir, PspImportFile file, String charsetName, char separator) throws ImportFileNotFoundException {
        try {
            return new CSVReader(new InputStreamReader(new FileInputStream(
                    new File(dir, file.getFileName())), charsetName), separator, CSVWriter.NO_QUOTE_CHARACTER, 1);
        } catch (Exception e) {
            throw new ImportFileNotFoundException(e, file.getFileName());
        }
    }

    private BufferedWriter getErrorFile(String dir, PspImportFile file) throws OpenErrorFileException {
        String name = file.getFileName().substring(0, file.getFileName().lastIndexOf(".")) + ERROR_FILE_SUFFIX;
        try {
            return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(dir, name)), CHARSET));
        } catch (Exception e) {
            throw new OpenErrorFileException(e, name);
        }
    }

    private BufferedWriter getErrorDescriptionFile(String dir, PspImportFile file) throws OpenErrorDescriptionFileException {
        String name = file.getFileName().substring(0, file.getFileName().lastIndexOf(".")) + ERROR_DESCRIPTION_FILE_SUFFIX;
        try {
            return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(dir, name)), CHARSET));
        } catch (Exception e) {
            throw new OpenErrorDescriptionFileException(e, name);
        }
    }
}
