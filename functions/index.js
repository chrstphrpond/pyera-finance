const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();
const db = admin.firestore();

/**
 * Triggered when a new user is created in Firebase Auth.
 * Creates default categories for the user in Firestore.
 */
exports.onUserCreate = functions.auth.user().onCreate(async (user) => {
    const userId = user.uid;
    const categoriesRef = db.collection("users").doc(userId).collection("categories");

    const defaultCategories = [
        { name: "Food", icon: "fastfood", color: -53759, type: "EXPENSE" }, // Red
        { name: "Transport", icon: "directions_car", color: -12268545, type: "EXPENSE" }, // Blue
        { name: "Shopping", icon: "shopping_bag", color: -21504, type: "EXPENSE" }, // Orange
        { name: "Salary", icon: "payments", color: -9834322, type: "INCOME" } // Green
    ];

    try {
        const batch = db.batch();
        defaultCategories.forEach((category) => {
            const docRef = categoriesRef.doc();
            batch.set(docRef, category);
        });
        await batch.commit();
        console.log(`Default categories created for user: ${userId}`);
    } catch (error) {
        console.error("Error creating default categories:", error);
    }
});

/**
 * Triggered when a transaction is created.
 * Logs high-value transactions (> 10,000) to 'audit_logs'.
 */
exports.auditLog = functions.firestore
    .document("users/{userId}/transactions/{transactionId}")
    .onCreate(async (snap, context) => {
        const newValue = snap.data();
        const amount = newValue.amount;
        const userId = context.params.userId;
        const transactionId = context.params.transactionId;

        // Threshold for high-value transaction
        if (amount > 10000) {
            const logEntry = {
                userId: userId,
                transactionId: transactionId,
                amount: amount,
                timestamp: admin.firestore.FieldValue.serverTimestamp(),
                reason: "High value transaction"
            };

            try {
                await db.collection("audit_logs").add(logEntry);
                console.log(`Audit log created for transaction: ${transactionId}`);
            } catch (error) {
                console.error("Error creating audit log:", error);
            }
        }
    });
