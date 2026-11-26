package quizbattle.net;

import quizbattle.dao.QuestionDAO;
import quizbattle.model.Question;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provides questions for games: tries MySQL database first,
 * and seamlessly falls back to bundled seed.sql if MySQL is not running.
 */
public class QuestionProvider {
    private static final QuestionDAO questionDAO = new QuestionDAO();

    public static List<Question> getQuestions(String category, int count) {
        try {
            List<Question> questions = (category == null || category.equalsIgnoreCase("All"))
                    ? questionDAO.findRandom(count)
                    : questionDAO.findRandomByCategory(category, count);
            if (!questions.isEmpty()) {
                return questions;
            }
        } catch (Exception ignored) {}

        List<Question> fallback = loadFromSeedFile(category);
        Collections.shuffle(fallback);
        if (fallback.size() > count) {
            return new ArrayList<>(fallback.subList(0, count));
        }
        return fallback;
    }

    public static List<String> getAvailableCategories() {
        try {
            List<String> categories = questionDAO.findCategories();
            if (!categories.isEmpty()) {
                return categories;
            }
        } catch (Exception ignored) {}

        return List.of("Science", "History", "Technology", "Geography", "General Knowledge");
    }

    private static List<Question> loadFromSeedFile(String targetCategory) {
        List<Question> list = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\('((?:''|[^'])*)',\\s*'((?:''|[^'])*)',\\s*'((?:''|[^'])*)',\\s*'((?:''|[^'])*)',\\s*'((?:''|[^'])*)',\\s*'([A-D])',\\s*'((?:''|[^'])*)'\\)");

        try (InputStream is = QuestionProvider.class.getClassLoader().getResourceAsStream("seed.sql")) {
            if (is == null) return list;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                int id = 1;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (!line.startsWith("(") || (!line.endsWith("),") && !line.endsWith(");"))) continue;
                    Matcher m = pattern.matcher(line);
                    if (m.find()) {
                        String text = m.group(1).replace("''", "'");
                        String a = m.group(2).replace("''", "'");
                        String b = m.group(3).replace("''", "'");
                        String c = m.group(4).replace("''", "'");
                        String d = m.group(5).replace("''", "'");
                        String correct = m.group(6);
                        String category = m.group(7).replace("''", "'");

                        if (targetCategory == null || targetCategory.equalsIgnoreCase("All")
                                || category.equalsIgnoreCase(targetCategory.trim())) {
                            int correctIdx = "ABCD".indexOf(correct);
                            list.add(new Question(id++, text, List.of(a, b, c, d), correctIdx, category));
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
        return list;
    }
}
