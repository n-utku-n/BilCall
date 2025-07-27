package com.project1;


import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.DocumentReference;
import com.google.firebase.cloud.FirestoreClient;

import java.io.InputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

public class AddCommentTest {

    private static final String EVENT_ID = "0wCZU1aaMHqiifOCmtcW";

    public static void main(String[] args) throws Exception {
        initFirebase();

        String[] eventIds = {"jUDkO5cyJLNxY9Hu8Rz9"};
        String[] userIds  = {"SchBzLGEoFbZIz4IlfPWMFQZo6U2", "nFD1jxRcawMVvPnC3PqvmBLolxx1", "rxzdA8TdsoO958GW1ZOLHTxm5722"};

        for (String eid : eventIds) {
            for (String uid : userIds) {
                addComment(eid, uid,
                    "My comment about the event (" + eid + ", " + uid + ")", 
                    1 + new java.util.Random().nextInt(5));
            }
        }

        System.out.println("Sample comments have been added");
    }

    private static void initFirebase() throws Exception {
        InputStream serviceAccount = AddCommentTest.class
            .getClassLoader()
            .getResourceAsStream("firebase/serviceAccountKey.json");
        if (serviceAccount == null) {
            throw new FileNotFoundException("Classpath resource serviceAccountKey.json not found");
        }
        FirebaseOptions options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
            .build();
        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options);
        }
    }

    private static void addComment(String eventId,
                                   String userId,
                                   String text,
                                   int rating) {
        Firestore db = FirestoreClient.getFirestore();
        CollectionReference commentsRef = db
            .collection("events")
            .document(eventId)
            .collection("comments");

        Map<String, Object> comment = new HashMap<>();
        comment.put("userId", userId);
        comment.put("text", text);
        comment.put("rating", rating);
        comment.put("timestamp", Timestamp.now());

        try {
            DocumentReference ref = commentsRef.add(comment).get();
            System.out.println("Comment added, ID: " + ref.getId());
        } catch (Exception e) {
            System.err.println("Failed to add comment: " + e.getMessage());
        }
    }
}
