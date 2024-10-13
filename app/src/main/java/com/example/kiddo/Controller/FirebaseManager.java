package com.example.kiddo.Controller;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import com.example.kiddo.Model.Child;
import com.example.kiddo.Model.Parent;
import com.example.kiddo.Model.Point;
import com.example.kiddo.Model.TaskInfo;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class FirebaseManager {
    private static final String TAG = "FirebaseManager";
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    public FirebaseManager() {
        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    public Task<AuthResult> createUserWithEmailAndPassword(String email, String password) {
        return firebaseAuth.createUserWithEmailAndPassword(email, password);
    }

    public Task<Void> saveChildToFirestore(String childName, String childEmail, String parentEmail, String childPassword) {
        // البحث عن الأب باستخدام البريد الإلكتروني
        return db.collection("parents")
                .whereEqualTo("email", parentEmail)
                .get()
                .continueWithTask(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        String parentId = task.getResult().getDocuments().get(0).getId(); // الحصول على معرف الأب

                        // تسجيل الابن في Firebase Authentication
                        return firebaseAuth.createUserWithEmailAndPassword(childEmail, childPassword) // استخدم كلمة مرور آمنة
                                .continueWithTask(task1 -> {
                                    if (task1.isSuccessful()) {
                                        String childId = firebaseAuth.getCurrentUser().getUid(); // الحصول على معرف الطفل

                                        // تخزين معلومات الطفل في Firestore
                                        Child child = new Child(childName, childEmail, parentId);
                                        return db.collection("children").document(childId)
                                                .set(child);
                                    } else {
                                        throw new Exception("فشل تسجيل الطفل: " + task1.getException().getMessage());
                                    }
                                });
                    } else {
                        throw new Exception("لم يتم العثور على الأب باستخدام البريد الإلكتروني");
                    }
                });
    }

    public void addParentToDatabase(String userId, String username, String email) {
        Parent parent = new Parent(username, email); // تأكد من أن لديك كلاس Parent
        DocumentReference parentRef = db.collection("parents").document(userId);

        parentRef.set(parent).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("FirebaseManager", "تم إضافة معلومات الأب بنجاح");
            } else {
                Log.d("FirebaseManager", "فشل إضافة معلومات الأب: " + task.getException().getMessage());
            }
        });
    }

    public Task<String> checkUserEmail(String email) {
        // تحقق من جدول الآباء
        TaskCompletionSource<String> taskCompletionSource = new TaskCompletionSource<>();

        db.collection("parents")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        // البريد الإلكتروني ينتمي للآباء
                        taskCompletionSource.setResult("parent");
                    } else {
                        // تحقق من جدول children
                        db.collection("children") // استخدام collectionGroup للبحث في جميع مجموعات children
                                .whereEqualTo("email", email)
                                .get()
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful() && !task1.getResult().isEmpty()) {
                                        // البريد الإلكتروني ينتمي لجدول children
                                        taskCompletionSource.setResult("child");
                                    } else {
                                        // البريد الإلكتروني غير موجود في أي جدول
                                        taskCompletionSource.setResult("not_registered");
                                    }
                                });
                    }

                });

        return taskCompletionSource.getTask();
    }

    public Task<String> signIn(String email, String password) {
        // تحقق مما إذا كان البريد الإلكتروني موجودًا في Firebase Authentication
        return firebaseAuth.signInWithEmailAndPassword(email, password)
                .continueWithTask(task -> {
                    if (task.isSuccessful()) {
                        // البريد الإلكتروني موجود في Authentication
                        return checkUserInFirestore(email);
                    } else {
                        throw task.getException();
                    }
                });
    }

    private Task<String> checkUserInFirestore(String email) {
        TaskCompletionSource<String> taskCompletionSource = new TaskCompletionSource<>();

        db.collection("parents")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        // البريد الإلكتروني ينتمي للآباء
                        taskCompletionSource.setResult("parent");
                    } else {
                        // تحقق من جدول الأطفال
                        db.collection("children")
                                .whereEqualTo("email", email)
                                .get()
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful() && !task1.getResult().isEmpty()) {
                                        // البريد الإلكتروني ينتمي لجدول الأطفال
                                        taskCompletionSource.setResult("children");
                                    } else {
                                        // البريد الإلكتروني غير موجود في أي جدول
                                        taskCompletionSource.setResult("not_registered");
                                    }
                                });
                    }
                });

        return taskCompletionSource.getTask();
    }

    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }

    public String getCurrentUserId() {
        FirebaseUser user = getCurrentUser();
        if (user != null) {
            return user.getUid(); // إرجاع معرف المستخدم
        }
        return null; // إرجاع null إذا لم يكن المستخدم مسجلاً الدخول
    }

    public void signOut() {
        firebaseAuth.signOut();
    }
//----------------------------


    // جلب الأطفال الذين يتبعون معرف الأب المحدد
    public Task<List<Child>> getChildrenByParentId(String parentId) {
        List<Child> childrenList = new ArrayList<>();
        TaskCompletionSource<List<Child>> taskCompletionSource = new TaskCompletionSource<>();

        db.collection("children") // افتراض أن الأطفال في مجموعة مستقلة
                .whereEqualTo("parentId", parentId) // تصفية الأطفال حسب معرف الأب
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            Child child = document.toObject(Child.class);
                            child.setId(document.getId()); // تعيين معرف الطفل
                            childrenList.add(child);
                        }
                        taskCompletionSource.setResult(childrenList);
                    } else {
                        taskCompletionSource.setException(task.getException());
                    }
                });

        return taskCompletionSource.getTask();
    }

    public void storeBedMakingInfo(Context context, TaskInfo taskInfo, Bitmap bitmap) {
        if (!isInternetAvailable(context)) {
            Toast.makeText(context, "لا يوجد اتصال بالإنترنت. يرجى التحقق من اتصالك.", Toast.LENGTH_SHORT).show();
            return; // عدم متابعة العملية إذا لم يكن هناك اتصال
        }

        // تحويل Bitmap إلى ByteArray
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos); // يمكن تغيير الجودة
        byte[] data = baos.toByteArray();
        String userId = getCurrentUserId();
        String date = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()); // تنسيق التاريخ
        String imageName = "bed_making_" + userId + "_" + date + ".jpg";
        // تخزين الصورة في Firebase Storage
        StorageReference storageRef = storage.getReference().child("images/bed_making/" + imageName);
        storageRef.putBytes(data)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(downloadUrl -> {
                    taskInfo.setImageUrl(downloadUrl.toString());

                    // تخزين المعلومات في Firestore
                    db.collection("children").document(userId)
                            .collection("tasks").document()
                            .set(taskInfo)
                            .addOnSuccessListener(aVoid ->
                                    storePointsInfo( new Point(userId,taskInfo.getPoints())))
                            .addOnFailureListener(e -> Log.w("FirebaseManager", "Error storing bed making info", e));
                }))
                .addOnFailureListener(e -> Log.w("FirebaseManager", "Error uploading image", e));
    }


    public void storeTaskInfo( TaskInfo taskInfo) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // الحصول على معرف المستخدم
        // تخزين المعلومات في Firestore
        db.collection("children").document(userId)
                .collection("tasks").document()
                .set(taskInfo)
                .addOnSuccessListener(aVoid -> {

                    storePointsInfo( new Point(userId,taskInfo.getPoints()));

                })
                .addOnFailureListener(e -> Log.w("FirebaseManager", "Error storing Hadith reading info", e));
    }
    public void storePointsInfo(Point point) {
        DocumentReference pointsRef = db.collection("points").document(point.getChildId());

        // استخدام الدالة set مع خيار merge
        pointsRef.set(new HashMap<String, Object>() {{
                    put("points", FieldValue.increment(point.getPointsNumber()));
                }}, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Log.d("FirebaseManager", "نقاط الطفل تم تحديثها بنجاح.");
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseManager", "فشل تحديث النقاط: " + e.getMessage());
                }); }

    public boolean isInternetAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
    // دالة لاسترجاع نقاط الطفل بشكل متزامن
    public Task<Long> getChildPoints(String childId) {
        DocumentReference pointsRef = db.collection("points").document(childId);
        return pointsRef.get().continueWith(task -> {
            // إذا كانت العملية ناجحة، استرجع النقاط
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    return document.getLong("points") != null ? document.getLong("points") : 0;
                }
            }
            // إذا لم يكن هناك مستند، أو كانت العملية غير ناجحة، ارجع 0
            return 0L;
        });
    }
}