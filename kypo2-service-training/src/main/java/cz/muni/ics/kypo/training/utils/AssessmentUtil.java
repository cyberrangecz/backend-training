package cz.muni.ics.kypo.training.utils;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.github.fge.jsonschema.main.JsonValidator;
import cz.muni.ics.kypo.training.exceptions.InternalServerErrorException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The type Assessment util.
 */
public class AssessmentUtil {

    private static Logger LOG = LoggerFactory.getLogger(AssessmentUtil.class);


    /**
     * Evaluate test int.
     *
     * @param questions the questions
     * @param responses the responses
     * @return the int
     */
    public static int evaluateTest(JSONArray questions, JSONArray responses) {
        LOG.info("Evaluating test");
        int receivedPoints = 0;

        for (int i = 0; i < responses.length(); i++) {
            JSONObject question = getQuestionWithOrder(questions, responses.getJSONObject(i).getInt("question_order"));
            if (question == null) {
                continue;
            }
            if (question.get("question_type").equals("FFQ")) {
                LOG.info("Evaluating FFQ question");
                String answer = responses.getJSONObject(i).getString("text");
                receivedPoints += evaluateFFQQuestion(question,answer);
            }
            else if (question.get("question_type").equals("MCQ")) {
                LOG.info("Evaluating MCQ question");
                JSONArray answers = responses.getJSONObject(i).getJSONArray("choices");
                receivedPoints += evaluateMCQQuestion(question, answers);
            }
            else if (question.get("question_type").equals("EMI")) {
                LOG.info("Evaluating EMI question");
                JSONArray answers = responses.getJSONObject(i).getJSONArray("pairs");
                receivedPoints += evaluateEMIQuestion(question,answers);
            }
        }
        return receivedPoints;

    }


    /**
     * Method converts JSONArray to List<Integer>.
     *
     * @param jsonArray the json array
     * @return list of integers
     */
    private static List<Integer> convertJSONArrayToListOfInt(JSONArray jsonArray) {
        List<Integer> userAnswers = new ArrayList<>();
        if (jsonArray.toList().isEmpty()) {
            return userAnswers;
        }
        for (int s = 0; s < jsonArray.length(); s++) {
            userAnswers.add(jsonArray.getInt(s));
        }
        return userAnswers;
    }

    /**
     * Method gets user's answer for EMI question.
     *
     * @param pairs list of individual answers to a specific question.
     * @return user mapping of choices
     */
    private static Map<Integer,Integer> getEMIAnswersFromUser(JSONArray pairs) {
        Map<Integer, Integer> userMapping = new HashMap<>();
        for (int k = 0; k < pairs.length(); k++) {
            JSONObject pairsXY = pairs.getJSONObject(k);
            userMapping.put(pairsXY.getInt("x"),pairsXY.getInt("y"));
        }
        return userMapping;
    }

    /**
     * Method gets correct choices for MCQ question.
     *
     * @param question to get correct choices
     * @return list of correct choices orders
     */
    private static List<Integer> getCorrectChoicesForMCQ (JSONObject question) {
        JSONArray allChoices = question.getJSONArray("choices");
        List<Integer> correctChoices = new ArrayList<>();
        for (int k = 0; k < allChoices.length(); k++) {
            if(allChoices.getJSONObject(k).getBoolean("is_correct")) {
                correctChoices.add(allChoices.getJSONObject(k).getInt("order"));
            }
        }
        return correctChoices;
    }

    /**
     * Method gets correct choice text for FFQ question.
     *
     * @param question to get correct choices
     * @return list of correct choices text
     */
    private static List<String> getCorrectChoicesForFFQ (JSONObject question) {
        JSONArray allChoices = question.getJSONArray("correct_choices");
        List<String> correctChoices = new ArrayList<>();
        for (int k = 0; k < allChoices.length(); k++) {
            correctChoices.add(allChoices.get(k).toString().toLowerCase());

        }
        return correctChoices;

    }

    /**
     * Method gets correct mapping of choices for EMI question.
     *
     * @param question to get correct mapping
     * @return map of correct choice mapping
     */
    private static Map<Integer, Integer> getCorrectChoicesForEMI (JSONObject question) {
        JSONArray allChoices = question.getJSONArray("correct_answers");
        Map<Integer,Integer> correctMapping = new HashMap<>();
        for (int k = 0; k < allChoices.length(); k++) {
            int key = allChoices.getJSONObject(k).getInt("x");
            int value = allChoices.getJSONObject(k).getInt("y");
            if (!correctMapping.containsKey(key) && !correctMapping.containsValue(key)) {
                correctMapping.put(key,value);
            }

        }
        return correctMapping;
    }

    /**
     * Method gets from assessment the question in given order.
     *
     * @param questions     JSONArray of questions
     * @param questionOrder in which question is in the assessment
     * @return question in the given order as JSONObject or null if there is no question in given order
     */
    private static JSONObject getQuestionWithOrder (JSONArray questions, int questionOrder) {
        for (int i = 0; i < questions.length(); i++) {
            if(questions.getJSONObject(i).getInt("order") == questionOrder) {
                return questions.getJSONObject(i);
            }
        }
        return null;
    }


    /**
     * Method evaluate a specific MCQ question.
     *
     * @param question which is evaluating
     * @param choices  marked by user
     * @return received points for the question
     */
    private static int evaluateMCQQuestion(JSONObject question, JSONArray choices) {
        List<Integer> correctMCQ = getCorrectChoicesForMCQ(question);
        List<Integer> userChoices = convertJSONArrayToListOfInt(choices);
        if (userChoices.containsAll(correctMCQ)) {
            return question.getInt("points");
        } else {
            return question.getInt("penalty")*(-1);
        }
    }

    /**
     * Method evaluate a specific FFQ question.
     *
     * @param question   which is evaluating
     * @param userAnswer answer from user
     * @return received points for the question
     */
    private static int evaluateFFQQuestion(JSONObject question, String userAnswer) {
        List<String> correctFFQ = getCorrectChoicesForFFQ(question);
        if (correctFFQ.contains(userAnswer.toLowerCase())) {
            return question.getInt("points");
        } else {
            return  question.getInt("penalty")*(-1);
        }
    }

    /**
     * Method evaluate a specific EMI question.
     *
     * @param question which is evaluating
     * @param answers  from user
     * @return received points for the question
     */
    private static int evaluateEMIQuestion(JSONObject question, JSONArray answers) {
        Map<Integer, Integer> userAnswers = getEMIAnswersFromUser(answers);
        Map<Integer, Integer> correctEMI = getCorrectChoicesForEMI(question);
        if (userAnswers.equals(correctEMI)) {
            return question.getInt("points");
        } else {
            return question.getInt("penalty")*(-1);
        }

    }

    /**
     * Method to get certain assessment from array of assessments.
     *
     * @param assessmentPosition the assessment position
     * @param assessmentOrder    the assessment order
     * @param assessments        list of assessments in JSON format
     * @return the assessment for responses
     */
    public JSONObject getAssessmentForResponses(int assessmentPosition, int assessmentOrder, JSONArray assessments) {
        for (int k = 0; k < assessments.length(); k++) {
            if (assessments.getJSONObject(k).getInt("position") == assessmentPosition
                    && assessments.getJSONObject(k).getInt("order") == assessmentOrder) {
                return assessments.getJSONObject(k);
            }
        }
        return null;
    }

    /**
     * Method put answers from single assessment to questions.
     *
     * @param answers   users answers for given questions
     * @param questions questions from certain assessment
     * @param userName  user who answer the question
     */
    public void putAnswersToQuestions(JSONArray answers, JSONArray questions, String userName) {
        for (int a = 0; a < answers.length(); a++) {
            int questionOrder = answers.getJSONObject(a).getInt("question_order");
            answers.getJSONObject(a).remove("question_order");
            answers.getJSONObject(a).put("userName", userName);
            if (questions.getJSONObject(questionOrder).isNull("answers")) {
                questions.getJSONObject(questionOrder).put("answers", new JSONArray("[" + answers.getJSONObject(a).toString() + "]"));
            } else {
                questions.getJSONObject(questionOrder).getJSONArray("answers").put(answers.getJSONObject(a));
            }
        }
    }

    /**
     * Method add responses to assessment.
     *
     * @param responses   responses from given user
     * @param userName    name of user
     * @param assessments list of assessment where to put responses
     */
    public void addResponsesToAssessments(JSONArray responses, String userName, JSONArray assessments) {
        for (int i = 0; i < responses.length(); i++) {
            int position = responses.getJSONObject(i).getInt("position");
            int order = responses.getJSONObject(i).getInt("order");
            JSONArray answers = responses.getJSONObject(i).getJSONArray("answers");
            putAnswersToQuestions(answers, getAssessmentForResponses(position,order,assessments).getJSONArray("questions"), userName);
        }

    }

    /**
     * Method which validate questions from assessment.
     *
     * @param questions to be validate
     * @throws IllegalArgumentException when questions are not valid.
     */
    public static void validQuestions(String questions) {
        try {
            JsonNode n = JsonLoader.fromString(questions);
            final JsonNode jsonSchema = JsonLoader.fromResource("/questions-schema.json");
            final JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
            JsonValidator v = factory.getValidator();
            ProcessingReport report = v.validate(jsonSchema, n);
            if (!report.toString().contains("success")) {
                throw new IllegalArgumentException("Given questions are not not valid .\n" + report.iterator().next());
            }

        } catch (IOException | ProcessingException ex) {
            throw new InternalServerErrorException(ex.getMessage());
        }
    }
}
