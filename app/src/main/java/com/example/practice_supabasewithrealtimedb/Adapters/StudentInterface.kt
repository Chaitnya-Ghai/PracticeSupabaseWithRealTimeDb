package com.example.practice_supabasewithrealtimedb.Adapters

import com.example.practice_supabasewithrealtimedb.StudentInfo

interface StudentInterface {
    fun Update(position:Int)
    fun deleteClick(studentInfo: StudentInfo,position: Int)
    fun itemClicked(position: Int,model: StudentInfo)
}
