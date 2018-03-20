package org.jax.pdxintegrator.rdf;


import com.github.phenomics.ontolib.ontology.data.ImmutableTermId;
import com.github.phenomics.ontolib.ontology.data.TermId;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.*;
import org.jax.pdxintegrator.model.PdxModel;
import org.jax.pdxintegrator.model.modelcreation.MouseTreatmentForEngraftment;
import org.jax.pdxintegrator.model.modelcreation.PdxModelCreation;
import org.jax.pdxintegrator.model.modelcreation.TumorPrepMethod;
import org.jax.pdxintegrator.model.patient.Consent;
import org.jax.pdxintegrator.model.patient.Gender;
import org.jax.pdxintegrator.model.patient.PdxPatient;
import org.jax.pdxintegrator.model.qualityassurance.ModelCharacterization;
import org.jax.pdxintegrator.model.qualityassurance.PdxQualityAssurance;
import org.jax.pdxintegrator.model.qualityassurance.ResponseToStandardOfCare;
import org.jax.pdxintegrator.model.tumor.PdxClinicalTumor;

import java.io.OutputStream;
import java.util.List;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.jax.pdxintegrator.model.modelstudy.PdxModelStudy;

/**
 * This class coordinates the transformation of one or more {@link org.jax.pdxintegrator.model.PdxModel} objects
 * as an RDF graph.
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 */
public class PdxModel2Rdf {

    private final List<PdxModel> pdxmodels;


    /** "Root" of the entire RDF graph. */
    //private Model rdfModel = ModelFactory.createDefaultModel();
    private OntModel rdfModel;
    
    

    public final TermId male = ImmutableTermId.constructWithPrefix("NCIT:C20197");
    public final TermId female = ImmutableTermId.constructWithPrefix("NCIT:C16576");

    // RDF properties needed throughout the model

   
    private Property hasPatientIdProperty=null;
    private Property hasSubmitterTumorIdProperty=null;
    private Property hasDiagnosisProperty=null;
    private Property hasTumorProperty=null;
    private Property hasTissueOfOriginProperty=null;
    private Property hasTumorCategoryProperty=null;
    private Property hasTumorHistologyProperty=null;
    private Property hasTumorGradeProperty=null;
    private Property hasStageProperty=null;
    private Property hasPdxModelProperty=null;
    private Property hasStrainProperty=null;
    private Property mouseSourceProperty=null;
    private Property strainHumanizedProperty=null;
    private Property humanizationTypeProperty=null;
    private Property tumorPreparation=null;
    private Property mouseTreatmentForEngraftment=null;
    private Property engraftmentPercentProperty=null;
    private Property engraftTimeInDaysProperty=null;
    private Property hasTumorCharacterizationProperty=null;
    private Property tumorNotEbvNotMouseProperty=null;
    private Property pdxTumorResponseProperty=null;
    private Property animalHealthStatusSatisfactoryProperty=null;
    private Property passageQaPerformedProperty=null;
    private Property currentTreatmentDrug=null;
    private Property ageBinLowerRange=null;
    private Property ageBinUpperRange=null;
    /** This property specifies the NCIT diagnosis of a patient's diagnosis. */
    private Property cancerDiagnosis=null;
    /** This property specifies the gender. ToDO decide on whether to use NCIT for this. */
    private Property genderProperty=null;
    /** This property specifies the consent given by the patient. TODO enough? */
    private Property consentProperty=null;
    /** This property specifies the population group of the patient. */
    private Property ethnicityProperty=null;
    
     private Property pdxStudyTreatmentProperty = null;
     private Property pdxDoublingLagTimeProperty = null;
     private Property pdxStudyHasMetastasisProperty = null;
     private Property pdxStudyMetastasisLocationProperty = null;
     private Property pdxStudyMetastasisPassageProperty = null;
     private Property pdxStudyTumorOmicsProperty = null;
    

    private Resource maleSex=null;
    private Resource femaleSex=null;
    private Resource noConsent=null;
    private Resource yesConsent=null;
    private Resource TRUE_RESOURCE=null;
    private Resource FALSE_RESOURCE=null;
    private Resource academicConsent=null;
    private Resource tumorSample=null;
    /** Tumor preparation types */
    private Resource tumorPrepSolid,tumorPrepSuspension,tumorPrepAscites;
    /** Mouse treatment types */
    private Resource mouseRxGCSF,mouseRxEstrogen;
    /** PDX Tumor response types */
    private Resource notAssessed, completeResponse, partialResponse, stableDisease, progressiveDisease;
    /** Tumor characterization types */
    private Resource IHC,histology;



    private Resource thisPatient=null;
    private Resource thisPdxModel=null;
    
    private Resource thisModelStudy= null;

    private static final String PDXNET_NAMESPACE = "http://pdxnetwork/pdxmodel_";
    private final static String NCIT_NAMESPACE = "http://purl.obolibrary.org/obo/NCIT_";
    private final static String UBERON_NAMESPACE ="http://purl.obolibrary.org/obo/UBERON_";


    /// these are the classes needed for the Seven Bridges model
    private OntClass pdxPatient;
    private OntClass pdxDiagnosis;
    private OntClass pdxSex;
    private OntClass pdxModelStudy;
    private OntClass pdxClinicalTumor;
    private OntClass pdxQualityAssurance;
    private OntClass pdxModelCreation;
    
    // these are a guess
    private OntClass pdxTreatmentResponse;
    private OntClass pdxTumorSampleType;
    private OntClass pdxModelCharacterization;
    
    // per adams email 
    private OntClass pdxPatientConsent;
    private OntClass pdxAnimalHealthStatus;
    private OntClass pdxTreatmentForEngraftment;
    private OntClass pdxNotEbvNotMouse;
    private OntClass pdxPassageQaPerformed;
    private OntClass pdxModelStage;
    private OntClass pdxStrainHumanized;
    private OntClass pdxTissueOfOrigin;
    private OntClass pdxTumorCategory;
    private OntClass pdxTumorGrade;
    private OntClass pdxTumorStage;

    
    public PdxModel2Rdf(List<PdxModel> modelList) {
        this.pdxmodels=modelList;
    }


    public void outputRDF(OutputStream out) {
        initializeModelFramework();
        specifyPrefixes();
        createEntities();
        for (PdxModel pdxmod : pdxmodels) {
            outputPdxModel(pdxmod);
        }
        System.out.println( "# -- PATIENT -- #" );
        rdfModel.write(System.out,"Turtle");
        //rdfModel.write(System.out,"RDF/XML");//// now write the model in XML form to a file
        //rdfModel.write(System.out, "RDF/XML-ABBREV");
        //rdfModel.write(System.out, "N-TRIPLES");
        rdfModel.write( out );//,"Turtle");
        
        
    }
    private void initializeModelFramework() {
        this.rdfModel = ModelFactory.createOntologyModel(  );
        
        String pdxMiPatientURI=String.format("%s%s",PDXNET_NAMESPACE,"PDX_MI_Patient");
        this.pdxPatient = rdfModel.createClass(pdxMiPatientURI);
        this.pdxPatient.addProperty(RDFS.label, "PDX-MI Patient");
        
        String pdxMiDiagnosisURI=String.format("%s%s",PDXNET_NAMESPACE,"PDX_MI_Diagnosis");
        this.pdxDiagnosis = rdfModel.createClass(pdxMiDiagnosisURI);
        this.pdxDiagnosis.addProperty(RDFS.label,"PDX-MI Diagnosis");
        
        String pdxMiSexURI=String.format("%s%s",PDXNET_NAMESPACE,"PDX_MI_Sex");
        this.pdxSex = rdfModel.createClass(pdxMiSexURI);
        this.pdxSex.addProperty(RDFS.label,"PDX-MI Sex");
        
        String pdxMiModelStudyURI=String.format("%s%s",PDXNET_NAMESPACE,"PDX_MI_ModelStudy");
        this.pdxModelStudy = rdfModel.createClass(pdxMiModelStudyURI);
        this.pdxModelStudy.addProperty(RDFS.label,"PDX-MI Model Study");
        
        String pdxMiClinicalTumorURI=String.format("%s%s",PDXNET_NAMESPACE,"PDX_MI_ClinicalTumor");
        this.pdxClinicalTumor = rdfModel.createClass(pdxMiClinicalTumorURI);
        this.pdxClinicalTumor.addProperty(RDFS.label,"PDX-MI Clinical Tumor");
        
        String pdxMiQualityAssuranceURI=String.format("%s%s",PDXNET_NAMESPACE,"PDX_MI_QualityAssurance");
        this.pdxQualityAssurance = rdfModel.createClass(pdxMiQualityAssuranceURI);
        this.pdxQualityAssurance.addProperty(RDFS.label,"PDX-MI Quality Assurance");
        
        String pdxMiModelCreationURI=String.format("%s%s",PDXNET_NAMESPACE,"PDX_MI_ModelCreation");
        this.pdxModelCreation = rdfModel.createClass(pdxMiModelCreationURI);
        this.pdxModelCreation.addProperty(RDFS.label,"PDX-MI Model Creation");
        
        // extending the guess
        String pdxMiTreatmentResponseURI=String.format("%s%s",PDXNET_NAMESPACE,"PDX_MI_TreatmentResponse");
        this.pdxTreatmentResponse = rdfModel.createClass(pdxMiTreatmentResponseURI);
        this.pdxTreatmentResponse.addProperty(RDFS.label,"PDX-MI Treatment Response");
        
        String pdxMiTumorSampleTypeURI=String.format("%s%s",PDXNET_NAMESPACE,"PDX_MI_TumorSampleType");
        this.pdxTumorSampleType = rdfModel.createClass(pdxMiTumorSampleTypeURI);
        this.pdxTumorSampleType.addProperty(RDFS.label,"PDX-MI Tumor Sample Type");
        
        String pdxMiModelCharacterizationURI=String.format("%s%s",PDXNET_NAMESPACE,"PDX_MI_ModelCharacterization");
        this.pdxModelCharacterization = rdfModel.createClass(pdxMiModelCharacterizationURI);
        this.pdxModelCharacterization.addProperty(RDFS.label,"PDX-MI Model Characterization");
        
        String pdxMiPatientConsentURI=String.format("%s%s",PDXNET_NAMESPACE,"PDX_MI_PatientConsent");
        this.pdxPatientConsent = rdfModel.createClass(pdxMiPatientConsentURI);
        this.pdxPatientConsent.addProperty(RDFS.label,"PDX-MI Patient Consent");

        String pdxMiAnimalHealthStatusURI = String.format("%s%s", PDXNET_NAMESPACE, "PDX_MI_AnimalHealthStatus");
        this.pdxAnimalHealthStatus = rdfModel.createClass(pdxMiAnimalHealthStatusURI);
        this.pdxAnimalHealthStatus.addProperty(RDFS.label, "PDX-MI Animal health status");

        String pdxMiTreatmentForEngraftmentURI = String.format("%s%s", PDXNET_NAMESPACE, "PDX_MI_TreatmentForEngraftment");
        this.pdxTreatmentForEngraftment = rdfModel.createClass(pdxMiTreatmentForEngraftmentURI);
        this.pdxTreatmentForEngraftment.addProperty(RDFS.label, "PDX-MI Mouse treatment for engraftment");

        String pdxMiNotEbvNotMouseURI = String.format("%s%s", PDXNET_NAMESPACE, "PDX_MI_NotEbvNotMouse");
        this.pdxNotEbvNotMouse = rdfModel.createClass(pdxMiNotEbvNotMouseURI);
        this.pdxNotEbvNotMouse.addProperty(RDFS.label, "PDX-MI Not Ebv Not Mouse");

        String pdxMiPassageQaPerformedURI = String.format("%s%s", PDXNET_NAMESPACE, "PDX_MI_PassageQaPerformed");
        this.pdxPassageQaPerformed = rdfModel.createClass(pdxMiPassageQaPerformedURI);
        this.pdxPassageQaPerformed.addProperty(RDFS.label, "PDX-MI Passage QA performed");

        String pdxMiModelStageURI = String.format("%s%s", PDXNET_NAMESPACE, "PDX_MI_ModelStage");
        this.pdxModelStage = rdfModel.createClass(pdxMiModelStageURI);
        this.pdxModelStage.addProperty(RDFS.label, "PDX-MI Model stage");

        String pdxMiStrainHumanizedURI = String.format("%s%s", PDXNET_NAMESPACE, "PDX_MI_StrainHumanized");
        this.pdxStrainHumanized = rdfModel.createClass(pdxMiStrainHumanizedURI);
        this.pdxStrainHumanized.addProperty(RDFS.label, "PDX-MI Strain humanized");

        String pdxMiTissueOfOriginURI = String.format("%s%s", PDXNET_NAMESPACE, "PDX_MI_TissueOfOrigin");
        this.pdxTissueOfOrigin = rdfModel.createClass(pdxMiTissueOfOriginURI);
        this.pdxTissueOfOrigin.addProperty(RDFS.label, "PDX-MI Tissue of origin");

        String pdxMiTumorCategoryURI = String.format("%s%s", PDXNET_NAMESPACE, "PDX_MI_TumorCategory");
        this.pdxTumorCategory = rdfModel.createClass(pdxMiTumorCategoryURI);
        this.pdxTumorCategory.addProperty(RDFS.label, "PDX-MI Tumor category");

        String pdxMiTumorGradeURI = String.format("%s%s", PDXNET_NAMESPACE, "PDX_MI_TumorGrade");
        this.pdxTumorGrade = rdfModel.createClass(pdxMiTumorGradeURI);
        this.pdxTumorGrade.addProperty(RDFS.label, "PDX-MI Tumor grade");
        
        String pdxMiTumorStageURI = String.format("%s%s", PDXNET_NAMESPACE, "PDX_MI_TumorStage");
        this.pdxTumorStage = rdfModel.createClass(pdxMiTumorStageURI);
        this.pdxTumorStage.addProperty(RDFS.label, "PDX-MI Tumor stage");
        
        
    }

    private void outputPdxModel(PdxModel pdxmodel) {

        // Clinincal/Patient Module
        outputPatientRDF(pdxmodel);
        // Clinical/Tumor Module
        outputTumorRDF(pdxmodel.getClinicalTumor());
        // Model Creation Module
        outputModelCreationRdf(pdxmodel);
        // Quality Assurance Module
        outputQualityAssuranceRdf(pdxmodel);
        // to do -- other areas of the PDX-MI
        //outputModelStudyRDF(pdxmodel);
    }



    /**
     * Todo add a suffix depending on the total number of diagnosis this patient has. For now just _01.
     */
    private void outputPatientRDF(PdxModel pdxmodel) {
        PdxPatient patient = pdxmodel.getPatient();
        String diagnosis = patient.getDiagnosis().getId();
        String diagnosisURI=String.format("%s%s",NCIT_NAMESPACE,diagnosis);
        String patientURI=String.format("%s%s",PDXNET_NAMESPACE,patient.getSubmitterPatientID());
        this.tumorSample = rdfModel.createResource(String.format("%s%s",PDXNET_NAMESPACE,pdxmodel.getClinicalTumor().getSubmitterTumorID()));
        Resource diagnosisResource = rdfModel.createResource(diagnosisURI);
        Resource sex = patient.getGender().equals(Gender.FEMALE) ? femaleSex : maleSex;
        Resource consent = patient.getConsent().equals(Consent.YES) ? yesConsent :
                patient.getConsent().equals(Consent.NO) ? noConsent : academicConsent;
        
        consent.addProperty(RDF.type, this.pdxPatientConsent);

        diagnosisResource.addProperty(RDF.type,this.pdxDiagnosis);

       this.thisPatient
                = rdfModel.createResource(patientURI)
                .addProperty(RDF.type,this.pdxPatient)
                .addProperty(hasPatientIdProperty,patient.getSubmitterPatientID())
                .addProperty(hasDiagnosisProperty, diagnosisResource)
                .addProperty(hasTumorProperty,tumorSample)
                .addProperty(genderProperty,sex)
                .addProperty(consentProperty,consent)
                .addProperty(ethnicityProperty,patient.getEthnicityRace().getEthnicityString());

        this.thisPatient.addProperty(ageBinLowerRange,
                ResourceFactory.createTypedLiteral(String.valueOf(patient.getAge().getLower()),
                        XSDDatatype.XSDinteger));
        this.thisPatient.addProperty(ageBinUpperRange,
                ResourceFactory.createTypedLiteral(String.valueOf(patient.getAge().getUpper()),
                        XSDDatatype.XSDinteger));

        this.thisPatient.addProperty(currentTreatmentDrug,
                ResourceFactory.createTypedLiteral(patient.getCurrentTreatmentDrug(),
                        XSDDatatype.XSDstring));
    }


    private void outputTumorRDF(PdxClinicalTumor clintumor) {
        String tumorURI=String.format("%s%s",PDXNET_NAMESPACE,clintumor.getSubmitterTumorID());
        
        Resource category = rdfModel.createResource(String.format("%s%s",NCIT_NAMESPACE,clintumor.getCategory().getId()));
        category.addProperty(RDF.type, this.pdxTumorCategory);
        
        Resource tissue = rdfModel.createResource(UBERON_NAMESPACE +clintumor.getTissueOfOrigin().getId());
        tissue.addProperty(RDF.type, this.pdxTissueOfOrigin);
        
        Resource histology = rdfModel.createResource(NCIT_NAMESPACE+clintumor.getTissueHistology().getId());
        // type?
        
        Resource stage = rdfModel.createResource(NCIT_NAMESPACE + clintumor.getStage().getId());
        stage.addProperty(RDF.type, this.pdxModelStage);
        
        Resource grade = rdfModel.createResource(NCIT_NAMESPACE + clintumor.getTumorGrade().getId());
        grade.addProperty(RDF.type, this.pdxTumorGrade);
        
        
        this.tumorSample.addProperty(hasSubmitterTumorIdProperty,clintumor.getSubmitterTumorID())
                .addProperty(hasTissueOfOriginProperty,tissue)
                .addProperty(hasTumorHistologyProperty,histology)
                .addProperty(hasStageProperty,stage)
                .addProperty(hasTumorGradeProperty,grade)
                .addProperty(hasTumorCategoryProperty,category);
        
        this.tumorSample.addProperty(RDF.type, this.pdxClinicalTumor);
    }
    
    private void outputModelStudyRDF(PdxModel model) {
        PdxModelStudy modelStudy = model.getModelStudy();
        // how is a model study identified?
        this.thisModelStudy = rdfModel.createResource(PDXNET_NAMESPACE + model.getModelCreation().getSubmitterPdxId());
        this.thisModelStudy.addProperty(hasPdxModelProperty,model.getModelCreation().getSubmitterPdxId());
        
        Resource tissue = rdfModel.createResource(UBERON_NAMESPACE +modelStudy.getMetastasisLocation().getId());
        // this is response to experimental treatment
        ResponseToStandardOfCare response = modelStudy.getTreatmentResponse();
        switch (response) {
            case NOT_ASSESSED: this.thisModelStudy.addProperty(pdxTumorResponseProperty,notAssessed); break;
            case STABLE_DISEASE: this.thisModelStudy.addProperty(pdxTumorResponseProperty,stableDisease); break;
            case PARTIAL_RESPONSE:this.thisModelStudy.addProperty(pdxTumorResponseProperty,partialResponse); break;
            case COMPLETE_RESPONSE:this.thisModelStudy.addProperty(pdxTumorResponseProperty,completeResponse); break;
            case PROGRESSIVE_DISEASE:this.thisModelStudy.addProperty(pdxTumorResponseProperty,progressiveDisease); break;
        }
        
        
        
        this.thisModelStudy.addProperty(pdxStudyTreatmentProperty,modelStudy.getTreatment())
                .addProperty(pdxDoublingLagTimeProperty,ResourceFactory.createTypedLiteral(String.valueOf(modelStudy.getDoublingLagTime()),
                        XSDDatatype.XSDinteger))
                .addProperty(pdxStudyHasMetastasisProperty,modelStudy.isMetastasis()?TRUE_RESOURCE : FALSE_RESOURCE)
                .addProperty(pdxStudyMetastasisLocationProperty,tissue)
                .addProperty(pdxStudyMetastasisPassageProperty,ResourceFactory.createTypedLiteral(String.valueOf(modelStudy.getMetastasisPassage()),
                        XSDDatatype.XSDinteger))
                .addProperty(pdxStudyTumorOmicsProperty,modelStudy.getTumorOmics().toString());
        
        this.thisModelStudy.addProperty(RDF.type, this.pdxModelStudy);
    }



    private void outputModelCreationRdf(PdxModel model ) {
        PdxModelCreation mcreation = model.getModelCreation();
        this.thisPdxModel = rdfModel.createResource(PDXNET_NAMESPACE + mcreation.getSubmitterPdxId());
        this.thisPatient.addProperty(hasPdxModelProperty,this.thisPdxModel);
        this.thisPdxModel.addProperty(hasStrainProperty,mcreation.getMouseStrain());
        this.thisPdxModel.addProperty(mouseSourceProperty,mcreation.getMouseSource());
        if (mcreation.isStrainImmuneSystemHumanized()) {
            this.thisPdxModel.addProperty(strainHumanizedProperty,TRUE_RESOURCE);
            this.thisPdxModel.addProperty(humanizationTypeProperty,mcreation.getHumanizationType());
        } else {
            this.thisPdxModel.addProperty(strainHumanizedProperty,FALSE_RESOURCE);
        }
        TumorPrepMethod prep = mcreation.getTumorPreparation();
        switch (prep) {
            case SOLID:this.thisPdxModel.addProperty(tumorPreparation,tumorPrepSolid);
            break;
            case ASCITES:this.thisPdxModel.addProperty(tumorPreparation,tumorPrepAscites);
            break;
            case SUSPENSION:this.thisPdxModel.addProperty(tumorPreparation,tumorPrepSuspension);
            break;
        }
        MouseTreatmentForEngraftment rx = mcreation.getMouseTreatmentForEngraftment();
        switch (rx) {
            case GCSF: this.thisPdxModel.addProperty(mouseTreatmentForEngraftment,mouseRxGCSF); break;
            case ESTROGEN: this.thisPdxModel.addProperty(mouseTreatmentForEngraftment,mouseRxEstrogen); break;
        }
        this.thisPdxModel.addProperty(engraftmentPercentProperty,
                ResourceFactory.createTypedLiteral(String.valueOf(mcreation.getEngraftmentRate()),
                XSDDatatype.XSDdecimal));
        this.thisPdxModel.addProperty(engraftTimeInDaysProperty,
                ResourceFactory.createTypedLiteral(String.valueOf(mcreation.getEngraftmentTimeInDays()),
                        XSDDatatype.XSDinteger));
        
        this.thisPdxModel.addProperty(RDF.type, this.pdxModelCreation);
    }


    private void outputQualityAssuranceRdf(PdxModel model) {
        PdxQualityAssurance quality = model.getQualityAssurance();
        ResponseToStandardOfCare response = quality.getResponse();
        switch (response) {
            case NOT_ASSESSED: this.thisPdxModel.addProperty(pdxTumorResponseProperty,notAssessed); break;
            case STABLE_DISEASE: this.thisPdxModel.addProperty(pdxTumorResponseProperty,stableDisease); break;
            case PARTIAL_RESPONSE:this.thisPdxModel.addProperty(pdxTumorResponseProperty,partialResponse); break;
            case COMPLETE_RESPONSE:this.thisPdxModel.addProperty(pdxTumorResponseProperty,completeResponse); break;
            case PROGRESSIVE_DISEASE:this.thisPdxModel.addProperty(pdxTumorResponseProperty,progressiveDisease); break;
        }
        ModelCharacterization characterization = quality.getTumorCharacterizationTechnology();
        switch (characterization) {
            case IHC: this.thisPdxModel.addProperty(hasTumorCharacterizationProperty,IHC);
            case HISTOLOGY:this.thisPdxModel.addProperty(hasTumorCharacterizationProperty,histology);
        }
        
        // do these FALSE and TRUE resources need to be typed seperately?
        if (quality.isAnimalHealthStatusSufficient()) {
            this.thisPdxModel.addProperty(animalHealthStatusSatisfactoryProperty,TRUE_RESOURCE);
        } else {
            this.thisPdxModel.addProperty(animalHealthStatusSatisfactoryProperty,FALSE_RESOURCE);
        }
        if (quality.isPassageQaPerformed()) {
            this.thisPdxModel.addProperty(passageQaPerformedProperty,TRUE_RESOURCE);
        } else {
            this.thisPdxModel.addProperty(passageQaPerformedProperty,FALSE_RESOURCE);
        }
        if (quality.isTumorNotMouseNotEbv()) {
            this.thisPdxModel.addProperty(tumorNotEbvNotMouseProperty,TRUE_RESOURCE);
        } else {
            this.thisPdxModel.addProperty(tumorNotEbvNotMouseProperty,FALSE_RESOURCE);
        }

    }




    private void createEntities() {
        this.maleSex=rdfModel.createResource(NCIT_NAMESPACE + male.getId());
        this.femaleSex = rdfModel.createResource( NCIT_NAMESPACE + female.getId() );
        this.maleSex.addProperty(RDF.type,this.pdxSex);
        this.maleSex.addProperty(RDFS.label,"Male sex");
        
        this.femaleSex.addProperty(RDF.type,this.pdxSex);
        this.femaleSex.addProperty(RDFS.label,"Female sex");
        
        // type patient consent?
        this.noConsent = rdfModel.createResource(PDXNET_NAMESPACE+"consent_NO");
        this.noConsent.addProperty(RDFS.label,"No patient consent provided");
        this.noConsent.addProperty(RDF.type,this.pdxPatientConsent);
        
        this.yesConsent = rdfModel.createResource(PDXNET_NAMESPACE+"consent_YES");
        this.yesConsent.addProperty(RDFS.label,"Patient consent provided");
        this.yesConsent.addProperty(RDF.type,this.pdxPatientConsent);
        
        this.academicConsent = rdfModel.createResource(PDXNET_NAMESPACE+"consent_ACADEMIC_ONLY");
        this.academicConsent.addProperty(RDFS.label,"Academic consent only");
        this.academicConsent.addProperty(RDF.type,this.pdxPatientConsent);
        
        this.TRUE_RESOURCE = rdfModel.createResource(PDXNET_NAMESPACE+"True");
        this.FALSE_RESOURCE = rdfModel.createResource(PDXNET_NAMESPACE+"False");
        
        //tumor solid, cell suspension, asite
        // type sample type?
        this.tumorPrepSolid = rdfModel.createResource(PDXNET_NAMESPACE+"Solid");
        this.tumorPrepSuspension = rdfModel.createResource(PDXNET_NAMESPACE+"Suspension");
        this.tumorPrepAscites = rdfModel.createResource(PDXNET_NAMESPACE+"Ascites");
        
        this.tumorPrepSolid.addProperty(RDFS.label, "Tumor preperation solid");
        this.tumorPrepSuspension.addProperty(RDFS.label, "Tumor preperation suspension");
        this.tumorPrepAscites.addProperty(RDFS.label, "Tumor preperation ascites");
        
        
        this.tumorPrepSolid.addProperty(RDF.type, this.pdxTumorSampleType);
        this.tumorPrepSuspension.addProperty(RDF.type, this.pdxTumorSampleType);
        this.tumorPrepAscites.addProperty(RDF.type, this.pdxTumorSampleType);
        
        // type humanization ?
        this.mouseRxGCSF = rdfModel.createResource(PDXNET_NAMESPACE+"G-CSF");
        this.mouseRxEstrogen = rdfModel.createResource(PDXNET_NAMESPACE+"Estrogen");

        // Are these all of a type "treatment response"?
        this.notAssessed= rdfModel.createResource(PDXNET_NAMESPACE+"Not_assessed");
        this.completeResponse= rdfModel.createResource(PDXNET_NAMESPACE+"Complete_response");
        this.partialResponse = rdfModel.createResource(PDXNET_NAMESPACE+"Partial_response");
        this.stableDisease = rdfModel.createResource(PDXNET_NAMESPACE+"Stable_disease");
        this.progressiveDisease = rdfModel.createResource(PDXNET_NAMESPACE+"Progressive_disease");
        
        
        this.notAssessed.addProperty(RDFS.label, "Not assessed");
        this.completeResponse.addProperty(RDFS.label, "Complete response");
        this.partialResponse.addProperty(RDFS.label, "Partial response");
        this.stableDisease.addProperty(RDFS.label, "Stable disease");
        this.progressiveDisease.addProperty(RDFS.label, "Progressive disease");
        
        // Guess con't
        this.notAssessed.addProperty(RDF.type, this.pdxTreatmentResponse);
        this.completeResponse.addProperty(RDF.type, this.pdxTreatmentResponse);
        this.partialResponse.addProperty(RDF.type, this.pdxTreatmentResponse);
        this.stableDisease.addProperty(RDF.type, this.pdxTreatmentResponse);
        this.progressiveDisease.addProperty(RDF.type, this.pdxTreatmentResponse);

        // type model characterization?
        this.IHC = rdfModel.createResource(PDXNET_NAMESPACE+"IHC");
        this.histology = rdfModel.createResource(PDXNET_NAMESPACE+"Histology");
        this.IHC.addProperty(RDF.type, this.pdxModelCharacterization);
        this.IHC.addProperty(RDFS.label,"IHC");
        
        this.histology.addProperty(RDF.type, this.pdxModelCharacterization);
        this.histology.addProperty(RDFS.label, "Histology");

    }


    private void specifyPrefixes() {
        // unique idenfitifer for the patient
        this.hasPatientIdProperty = rdfModel.createProperty(PDXNET_NAMESPACE + "patient_id");
        this.hasPatientIdProperty.addProperty(RDFS.label, "Unique identifer for patient");
        this.hasPatientIdProperty.addProperty(RDF.type,OWL.DatatypeProperty);
        
        // Patient's Initial Clinical Diagnosis
        this.hasDiagnosisProperty = rdfModel.createProperty( PDXNET_NAMESPACE + "hasDiagnosis" );
        this.hasDiagnosisProperty.addProperty(RDFS.label, "Patient's initial clincal diagnosis");
        this.hasDiagnosisProperty.addProperty(RDF.type,OWL.ObjectProperty);
        this.hasDiagnosisProperty.addProperty(RDFS.domain,this.pdxPatient);
        this.hasDiagnosisProperty.addProperty(RDFS.range,this.pdxDiagnosis);
        
        // Unique tumor id
        this.hasSubmitterTumorIdProperty = rdfModel.createProperty(PDXNET_NAMESPACE + "hasSubmitterTumorId");
        this.hasSubmitterTumorIdProperty.addProperty(RDFS.label, "Unique identifer for sampled tissue");
        this.hasSubmitterTumorIdProperty.addProperty(RDF.type, OWL.DatatypeProperty );
        
        // Tumor Sample Object
        this.hasTumorProperty = rdfModel.createProperty(PDXNET_NAMESPACE,"hasTumor");
        this.hasTumorProperty.addProperty(RDF.type,OWL.ObjectProperty);
        
        // Not sure what this is, it isn't used.
        //this.cancerDiagnosis = rdfModel.createProperty( PDXNET_NAMESPACE + "cancerDiagnosis" );
        //this.cancerDiagnosis.addProperty(RDFS.label, "");
       // this.cancerDiagnosis.addProperty(RDF.type,OWL.ObjectProperty);
        
        // Patient gender
        this.genderProperty = rdfModel.createProperty(PDXNET_NAMESPACE,"sex");
        this.genderProperty.addProperty(RDFS.label, "Patient Sex");
        this.genderProperty.addProperty(RDF.type,OWL.ObjectProperty);
        this.genderProperty.addProperty(RDFS.range,this.pdxSex);
        
        // Patient has provided consent to share data
        this.consentProperty = rdfModel.createProperty(PDXNET_NAMESPACE,"consent");
        this.consentProperty.addProperty(RDFS.label, "Patient consent to share data");
        this.consentProperty.addProperty(RDF.type, OWL.ObjectProperty );
        
        // Ethnicity of patient
        this.ethnicityProperty = rdfModel.createProperty(PDXNET_NAMESPACE,"ethnicity");
        this.ethnicityProperty.addProperty(RDFS.label, "Patient ethnicity");
        this.ethnicityProperty.addProperty(RDF.type, OWL.DatatypeProperty );
        
        // Patient tumor tissue of origin
        this.hasTissueOfOriginProperty = rdfModel.createProperty(PDXNET_NAMESPACE,"tissueOfOrigin");
        this.hasTissueOfOriginProperty.addProperty(RDFS.label, "Tumor's tissue of origin");
        this.hasTissueOfOriginProperty.addProperty(RDF.type, OWL.ObjectProperty );
        
        // Tumor Category
        this.hasTumorCategoryProperty = rdfModel.createProperty(PDXNET_NAMESPACE,"tumorCategory");
        this.hasTumorCategoryProperty.addProperty(RDFS.label, "Tumor category");
        this.hasTumorCategoryProperty.addProperty(RDF.type, OWL.ObjectProperty );
        
        // Histology for tumor
        this.hasTumorHistologyProperty = rdfModel.createProperty(PDXNET_NAMESPACE,"tumorHistology");
        this.hasTumorHistologyProperty.addProperty(RDFS.label,"Pathologist's Histologic Diagnosis");
        this.hasTumorHistologyProperty.addProperty(RDF.type, OWL.ObjectProperty);
        
        // Grade of tumor
        this.hasTumorGradeProperty = rdfModel.createProperty(PDXNET_NAMESPACE,"tumorGrade");
        this.hasTumorGradeProperty.addProperty(RDFS.label,"Tumor grade");
        this.hasTumorGradeProperty.addProperty(RDF.type, OWL.ObjectProperty);
        
        // Stage of tumor
        this.hasStageProperty = rdfModel.createProperty(PDXNET_NAMESPACE,"stage");
        this.hasStageProperty.addProperty(RDFS.label,"Tumor stage");
        this.hasStageProperty.addProperty(RDF.type, OWL.ObjectProperty);
        
        // PDX model generated from patient tumor
        this.hasPdxModelProperty = rdfModel.createProperty(PDXNET_NAMESPACE,"hasPdxModel");
        this.hasPdxModelProperty.addProperty(RDFS.label,"PDX model generated from patient tissue");
        this.hasPdxModelProperty.addProperty(RDF.type, OWL.ObjectProperty);
        
        // Mouse strain for PDX model
        this.hasStrainProperty = rdfModel.createProperty(PDXNET_NAMESPACE,"hasStrain");
        this.hasStrainProperty.addProperty(RDFS.label,"Mouse strain used for engraftment");
        this.hasStrainProperty.addProperty(RDF.type, OWL.DatatypeProperty);
        
        // Is the mouse strain humanized
        this.strainHumanizedProperty = rdfModel.createProperty(PDXNET_NAMESPACE,"strainHumanized");
        this.strainHumanizedProperty.addProperty(RDFS.label,"Was mouse strain humanized");
        this.strainHumanizedProperty.addProperty(RDF.type, OWL.ObjectProperty);
        
        // Type of mouse humanization
        this.humanizationTypeProperty = rdfModel.createProperty(PDXNET_NAMESPACE,"humanizationType");
        this.humanizationTypeProperty.addProperty(RDFS.label,"Type of mouse humanization");
        this.humanizationTypeProperty.addProperty(RDF.type, OWL.ObjectProperty);
        
        // Type of tumor preperation
        this.tumorPreparation = rdfModel.createProperty(PDXNET_NAMESPACE,"tumorPreparation");
        this.tumorPreparation.addProperty(RDFS.label,"Type of tumor preparation for engraftment");
        this.tumorPreparation.addProperty(RDF.type, OWL.ObjectProperty);
        
        // Instituion providing mouse
        this.mouseSourceProperty = rdfModel.createProperty(PDXNET_NAMESPACE,"mouseSource");
        this.mouseSourceProperty.addProperty(RDFS.label,"Institution providing mouse");
        this.mouseSourceProperty.addProperty(RDF.type, OWL.DatatypeProperty);
        
        // Treatement of mouse prior to engraftment
        this.mouseTreatmentForEngraftment = rdfModel.createProperty(PDXNET_NAMESPACE,"mouseTreatmentForEngraftment");
        this.mouseTreatmentForEngraftment.addProperty(RDFS.label,"Mouse treatment for engraftment");
        this.mouseTreatmentForEngraftment.addProperty(RDF.type, OWL.ObjectProperty);
        
        // Percent of successful engraftments 
        this.engraftmentPercentProperty = rdfModel.createProperty(PDXNET_NAMESPACE,"engraftmentPercent");
        this.engraftmentPercentProperty.addProperty(RDFS.label,"Engraftment percent");
        this.engraftmentPercentProperty.addProperty(RDF.type, OWL.DatatypeProperty);
        
        // Days for successful engraftment
        this.engraftTimeInDaysProperty = rdfModel.createProperty(PDXNET_NAMESPACE,"engraftmentTimeInDays");
        this.engraftTimeInDaysProperty.addProperty(RDFS.label,"Engraftment time in days");
        this.engraftTimeInDaysProperty.addProperty(RDF.type, OWL.DatatypeProperty);
        
        // Tumor Characterization
        this.hasTumorCharacterizationProperty= rdfModel.createProperty(PDXNET_NAMESPACE,"pdxTumorCharacterization");
        this.hasTumorCharacterizationProperty.addProperty(RDFS.label,"Tumor Characterization");
        this.hasTumorCharacterizationProperty.addProperty(RDF.type, OWL.ObjectProperty);
        
        // Tumor is not EBV or mouse tissue
        this.tumorNotEbvNotMouseProperty= rdfModel.createProperty(PDXNET_NAMESPACE,"notEbvNotMouse");
        this.tumorNotEbvNotMouseProperty.addProperty(RDFS.label,"Mouse is not EBV positive, tumor tissue is not mouse origin");
        this.tumorNotEbvNotMouseProperty.addProperty(RDF.type, OWL.ObjectProperty);
        
        // PDX model response to treatment
        this.pdxTumorResponseProperty= rdfModel.createProperty(PDXNET_NAMESPACE,"pdxTumorResponse");
        this.pdxTumorResponseProperty.addProperty(RDFS.label,"Tumor Response");
        this.pdxTumorResponseProperty.addProperty(RDF.type, OWL.ObjectProperty);
        
        // The pdx model's health status
        this.animalHealthStatusSatisfactoryProperty= rdfModel.createProperty(PDXNET_NAMESPACE,"animalHealthStatusOk");
        this.animalHealthStatusSatisfactoryProperty.addProperty(RDFS.label,"Animal health status is ok");
        this.animalHealthStatusSatisfactoryProperty.addProperty(RDF.type, OWL.ObjectProperty);
        
        // Passage on which QA was performed
        this.passageQaPerformedProperty= rdfModel.createProperty(PDXNET_NAMESPACE,"passageQAperformed");
        this.passageQaPerformedProperty.addProperty(RDFS.label,"Passage QA performed");
        this.passageQaPerformedProperty.addProperty(RDF.type, OWL.ObjectProperty);
        
        // Patient current treatment drug
        this.currentTreatmentDrug = rdfModel.createProperty(PDXNET_NAMESPACE,"currentTreatmentDrug");
        this.currentTreatmentDrug.addProperty(RDFS.label,"Current patient treatment drug");
        this.currentTreatmentDrug.addProperty(RDF.type, OWL.DatatypeProperty);
       
        // Lower age range for Patient when sample was taken
        this.ageBinLowerRange = rdfModel.createProperty(PDXNET_NAMESPACE,"ageBinLowerRange");
        this.ageBinLowerRange.addProperty(RDFS.label,"Lower range of 5 year age bin");
        this.ageBinLowerRange.addProperty(RDFS.domain,pdxPatient);
        this.ageBinLowerRange.addProperty(RDF.type, OWL.DatatypeProperty);
        
        // Upper age range for Patient when sample was taken
        this.ageBinUpperRange = rdfModel.createProperty(PDXNET_NAMESPACE,"ageBinUpperRange");
        this.ageBinUpperRange.addProperty(RDFS.label,"Upper range of 5 year age bin");
        this.ageBinUpperRange.addProperty(RDFS.domain,pdxPatient);
        this.ageBinUpperRange.addProperty(RDF.type, OWL.DatatypeProperty);
        
        this.pdxStudyTreatmentProperty = rdfModel.createProperty(PDXNET_NAMESPACE,"studyTreatment");
        this.pdxStudyTreatmentProperty.addProperty(RDFS.label,"Study treatment");
        this.pdxStudyTreatmentProperty.addProperty(RDF.type, OWL.ObjectProperty);
        
        this.pdxDoublingLagTimeProperty = rdfModel.createProperty(PDXNET_NAMESPACE,"doublingLagTime");
        this.pdxDoublingLagTimeProperty.addProperty(RDFS.label,"Doubling lag time");
        this.pdxDoublingLagTimeProperty.addProperty(RDF.type, OWL.ObjectProperty);
        
        this.pdxStudyHasMetastasisProperty = rdfModel.createProperty(PDXNET_NAMESPACE,"modelHasMetastasis");
        this.pdxStudyHasMetastasisProperty.addProperty(RDFS.label,"Study model has metastasis");
        this.pdxStudyHasMetastasisProperty.addProperty(RDF.type, OWL.ObjectProperty);
        
        this.pdxStudyMetastasisLocationProperty = rdfModel.createProperty(PDXNET_NAMESPACE,"modelMetastasisLocation");
        this.pdxStudyMetastasisLocationProperty.addProperty(RDFS.label,"Model metastasis location");
        this.pdxStudyMetastasisLocationProperty.addProperty(RDF.type, OWL.ObjectProperty);
        
        this.pdxStudyMetastasisPassageProperty = rdfModel.createProperty(PDXNET_NAMESPACE,"modelMetastasisPassage");
        this.pdxStudyMetastasisPassageProperty.addProperty(RDFS.label,"Model metastasis passage");
        this.pdxStudyMetastasisPassageProperty.addProperty(RDF.type, OWL.ObjectProperty);
        
        this.pdxStudyTumorOmicsProperty = rdfModel.createProperty(PDXNET_NAMESPACE,"tumorOmics");
        this.pdxStudyTumorOmicsProperty.addProperty(RDFS.label,"Study tumor omics");
        this.pdxStudyTumorOmicsProperty.addProperty(RDF.type, OWL.ObjectProperty);
        
        
     
        rdfModel.setNsPrefix( "PDXNET", PDXNET_NAMESPACE);
        rdfModel.setNsPrefix( "NCIT", NCIT_NAMESPACE);
        rdfModel.setNsPrefix("UBERON",UBERON_NAMESPACE);
        
    }



}
