package com.example.voltorbflipmobile;

import android.util.Log;

import java.util.Objects;

public class Handlers {
    public static final ExceptionHandler ARITHMETIC_EXCEPTION = e ->
            Log.d("Arithmetic Exception", Objects.requireNonNull(e.getMessage()));

    public static final ExceptionHandler NULL_POINTER_EXCEPTION = e ->
            Log.e("Null Pointer Exception", "Null value encountered: " + e.getClass().getSimpleName() + " - " + e.getMessage());

    public static final ExceptionHandler IO_EXCEPTION = e ->
            Log.w("IO Exception", "I/O operation failed: " + e.getClass().getSimpleName() + " - " + e.getMessage());

    public static final ExceptionHandler GENERAL_EXCEPTION = e ->
            Log.e("General Exception", "Error: " + e.getClass().getSimpleName() + " - " + e.getMessage());

    public static final ExceptionHandler RUNTIME_EXCEPTION = e ->
            Log.e("Runtime Exception", "Runtime error: " + e.getClass().getSimpleName() + " - " + e.getMessage());
}

