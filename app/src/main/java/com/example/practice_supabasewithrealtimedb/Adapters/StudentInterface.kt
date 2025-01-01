package com.example.practice_supabasewithrealtimedb.Adapters

import com.example.practice_supabasewithrealtimedb.StudentInfo

interface StudentInterface {
    fun UpdateOrDelete(position:Int,model:StudentInfo,deletekey:Int)
    fun itemClicked(position: Int,model: StudentInfo)
}
