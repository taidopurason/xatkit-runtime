package com.xatkit.core.recognition.nlpjs.mapper;


import com.google.protobuf.Value;
import com.xatkit.core.EventDefinitionRegistry;
import com.xatkit.core.recognition.nlpjs.NlpjsConfiguration;
import com.xatkit.core.recognition.nlpjs.NlpjsHelper;
import com.xatkit.core.recognition.nlpjs.model.*;
import com.xatkit.intent.*;
import fr.inria.atlanmod.commons.log.Log;
import lombok.NonNull;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;

import static com.xatkit.core.recognition.IntentRecognitionProvider.DEFAULT_FALLBACK_INTENT;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public class NlpjsRecognitionResultMapper {

    private NlpjsConfiguration configuration;

    private EventDefinitionRegistry eventRegistry;

    private NlpjsEntityReferenceMapper nlpjsEntityReferenceMapper;

    public NlpjsRecognitionResultMapper(@NonNull NlpjsConfiguration configuration,
                                  @NonNull EventDefinitionRegistry eventRegistry,
                                        @NonNull NlpjsEntityReferenceMapper nlpjsEntityReferenceMapper) {
        this.configuration = configuration;
        this.eventRegistry = eventRegistry;
        this.nlpjsEntityReferenceMapper = nlpjsEntityReferenceMapper;
    }

    public List<RecognizedIntent> mapRecognitionResult(@NonNull RecognitionResult recognitionResult){
        List<Classification> classifications = recognitionResult.getClassifications();
        List<RecognizedIntent> recognizedIntents = new ArrayList<>();
        for(Classification classification: classifications){
            RecognizedIntent recognizedIntent = IntentFactory.eINSTANCE.createRecognizedIntent();
            IntentDefinition intentDefinition = convertNlpjsIntentNameToIntentDefinition(classification.getIntent());
            recognizedIntent.setDefinition(intentDefinition);
            recognizedIntent.setRecognitionConfidence(classification.getScore());
            recognizedIntent.setMatchedInput(recognitionResult.getUtterance());
            recognizedIntents.add(recognizedIntent);
        }

        return  recognizedIntents;
    }

    public List<ContextInstance> mapParamterValues(RecognizedIntent recognizedIntent, List<ExtractedEntity> extractedEntities) {
        List<ContextInstance> contextInstances = new ArrayList<>();
        for (ExtractedEntity extractedEntity: extractedEntities) {
            String entityType = extractedEntity.getEntity();
            Context contextDefinition = NlpjsHelper.getContextFromNlpEntity(entityType, recognizedIntent.getDefinition().getOutContexts(),
                    nlpjsEntityReferenceMapper);
            if (nonNull(contextDefinition)) {
                ContextInstance contextInstance = IntentFactory.eINSTANCE.createContextInstance();
                contextInstance.setDefinition(contextDefinition);
                contextInstance.setLifespanCount(1);
                ContextParameter contextParameter = NlpjsHelper.getContextParameterFromNlpEntity(entityType, recognizedIntent.getDefinition().getOutContexts(),
                        nlpjsEntityReferenceMapper);
                if (nonNull(contextParameter) ) {
                    ContextParameterValue contextParameterValue =
                            IntentFactory.eINSTANCE.createContextParameterValue();
                    if (nonNull(extractedEntity.getOption())) {
                        contextParameterValue.setValue(extractedEntity.getOption());
                    } else if (nonNull(extractedEntity.getResolution()) && nonNull(extractedEntity.getResolution().getValue())) {
                        contextParameterValue.setValue(convertParameterValueToString(extractedEntity.getResolution()));
                    } else {
                        Log.warn("Cannot retrieve the value for the context parameter {0}", contextParameter.getName());
                    }
                    contextParameterValue.setContextParameter(contextParameter);
                    contextInstance.getValues().add(contextParameterValue);
                }
                contextInstances.add(contextInstance);

            }
        }
        return contextInstances;
    }

    private IntentDefinition convertNlpjsIntentNameToIntentDefinition(@NonNull String intentName) {
        if(intentName.equals("None")) {
            return DEFAULT_FALLBACK_INTENT;
        }
        IntentDefinition result = eventRegistry.getIntentDefinition(intentName);
        if (isNull(result)) {
            Log.warn("Cannot retrieve the {0} with the provided name {1}, returning the Default Fallback Intent",
                    IntentDefinition.class.getSimpleName(),intentName);
            result = DEFAULT_FALLBACK_INTENT;
        }
        return result;
    }

    private String convertParameterValueToString(@NonNull Resolution resolution) {
        Object value = resolution.getValue();
        if (value instanceof String)
            return (String) value;
        if (value instanceof Number) {
            DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
            decimalFormatSymbols.setDecimalSeparator('.');
            DecimalFormat decimalFormat = new DecimalFormat("0.###", decimalFormatSymbols);
            decimalFormat.setGroupingUsed(false);
            return decimalFormat.format(value);
        }
        Log.error("Cannot convert the provided value {0}", resolution);
        return "";
    }


}
