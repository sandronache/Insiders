//package main.java.util;
//import main.java.entity.Comment;
//import main.java.entity.Post;
//
//public final class Helper {
//    private Helper() {}
//
//    // indexes
//    public static int extractFirstLevel(String input) {
//        if (input.length() == 1) {
//            return Integer.parseInt(input);
//        }
//        int dot = input.indexOf('.');
//        return Integer.parseInt(input.substring(0, dot));
//    }
//
//    public static String extractRemainingLevels(String input) {
//        if (input.length() == 1) {
//            return "";
//        }
//        int dot = input.indexOf('.');
//        return input.substring(dot + 1);
//    }
//
//    public static boolean isCommentIdValid(String input) {
//        return input.matches("(\\d\\.)*\\d");
//    }
//
//    // passwords
//
//    public static boolean checkPassword(String password, int hash) {
//        return hash == password.hashCode();
//    }
//
//    public static int hashFunction(String password) {
//        return password.hashCode();
//    }
//
//    public static Comment findCommentById(Post post, String id) {
//        int topLevelIndex = extractFirstLevel(id);
//        Comment comment = post.getComments().get(topLevelIndex);
//        if (comment == null) return null;
//
//        String rest = extractRemainingLevels(id);
//        if (rest.isEmpty()) return comment;
//
//        return findCommentByIdRecursive(comment, rest);
//    }
//
//    private static Comment findCommentByIdRecursive(Comment comment, String id) {
//        int idx = extractFirstLevel(id);
//        Comment reply = comment.getReplies().get(idx);
//        if (reply == null) return null;
//
//        String rest = extractRemainingLevels(id);
//        return rest.isEmpty() ? reply : findCommentByIdRecursive(reply, rest);
//    }
//}