package com.example.practice_supabasewithrealtimedb

import android.app.Application
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage

class MyApplication:Application() {
    lateinit var supabaseClient:SupabaseClient
    override fun onCreate() {
        super.onCreate()
        supabaseClient= createSupabaseClient(
            "https://gjawsgukbhejbmlwrloy.supabase.co",
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImdqYXdzZ3VrYmhlamJtbHdybG95Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzQ5MzgzMDMsImV4cCI6MjA1MDUxNDMwM30.oIdUjjCVUQrdOcEkn4BWr7GTVVq6knzVXJRttFiqwTc")
        {
            install(Storage)
        }
        val bucket=supabaseClient.storage.from("studentBucket")
    }
}