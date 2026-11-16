package com.example.checkoutjava;

// Placeholders for your own UI. The examples call them; they are not part of the SDK.
final class UiPlaceholders {
    private UiPlaceholders() {
    }

    static void showPaymentOutcome(MerchantBackend.PaymentStatus status) {
        // Show success, pending or failure as your backend reported it.
    }

    static void showStatusUnknown() {
        // The outcome is unknown, not failed: tell the customer to check the order later.
    }
}
