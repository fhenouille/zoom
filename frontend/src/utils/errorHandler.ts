// Global error handler for debugging
export function setupGlobalErrorHandling() {
  // Log unhandled errors
  window.addEventListener('error', (event) => {
    console.error('❌ Global Error:', {
      message: event.message,
      filename: event.filename,
      lineno: event.lineno,
      colno: event.colno,
      error: event.error,
    });
  });

  // Log unhandled promise rejections
  window.addEventListener('unhandledrejection', (event) => {
    console.error('❌ Unhandled Promise Rejection:', event.reason);
  });

  // Log component errors
  console.log('✅ Global error handlers initialized');
}
