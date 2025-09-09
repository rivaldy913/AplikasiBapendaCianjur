package com.example.bapendacjrapp

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

val supabase = createSupabaseClient(
    supabaseUrl = "https://wlujtqiswyubhdjqcnav.supabase.co",
    supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6IndsdWp0cWlzd3l1YmhkanFjbmF2Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTM0MDkxMzAsImV4cCI6MjA2ODk4NTEzMH0.vuNI880TSDImPENiQNhclfytAlFzRpVhgAHBGRa2D0c"
) {
    install(Postgrest)
    install(Storage)
}