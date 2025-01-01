@file:Suppress("NAME_SHADOWING")

package com.example.practice_supabasewithrealtimedb.fragments

import android.annotation.SuppressLint
import android.app.ActionBar
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.practice_supabasewithrealtimedb.Adapters.StudentAdapter
import com.example.practice_supabasewithrealtimedb.Adapters.StudentInterface
import com.example.practice_supabasewithrealtimedb.MainActivity
import com.example.practice_supabasewithrealtimedb.MyApplication
import com.example.practice_supabasewithrealtimedb.R
import com.example.practice_supabasewithrealtimedb.StudentInfo
import com.example.practice_supabasewithrealtimedb.databinding.CustomDialogBinding
import com.example.practice_supabasewithrealtimedb.databinding.FragmentStudentBinding
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.UploadStatus
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.storage.uploadAsFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [StudentFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@SuppressLint("NotifyDataSetChanged")
class StudentFragment : Fragment(),StudentInterface{
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentStudentBinding
//    real time data base reference / instance created
    private var dbReference:DatabaseReference=FirebaseDatabase.getInstance().reference
    private lateinit var mainActivity: MainActivity
    private var array= arrayListOf<StudentInfo>()
    var studentAdapter: StudentAdapter?=StudentAdapter(this,array,this)
    private lateinit var linearLayoutManger: LinearLayoutManager
//supabase
    lateinit var supabaseClient: SupabaseClient
    private val PICK_IMAGE_REQUEST = 1
    private val PERMISSION_REQUEST_CODE = 100
    private val MANAGE_EXTERNAL_STORAGE_REQUEST_CODE = 101
// image variables
    lateinit var imgUri:Uri
    var publicUrl:String?=null
//
    lateinit var dialogBinding:CustomDialogBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainActivity=activity as MainActivity
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
//        always done in onCreate fun ->
        dbReference.addChildEventListener(object  : ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val studentInfo :StudentInfo?=snapshot.getValue(StudentInfo::class.java)
                studentInfo?.id = snapshot.key.toString()
                if (studentInfo!=null){
                    array.add(studentInfo)
                    studentAdapter?.notifyDataSetChanged()
                }
            }
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val studentInfo:StudentInfo?=snapshot.getValue(StudentInfo::class.java)
                studentInfo?.id=snapshot.key.toString()
                array.forEachIndexed { index, studentData ->
                    if (studentData!=null){
                        if (studentData.id==snapshot.key){
                            array[index]=studentData
                            studentAdapter?.notifyDataSetChanged()
                        }
                        return@forEachIndexed
                    }
                }
                mainActivity.binding.loading.visibility = View.GONE
            }
            override fun onChildRemoved(snapshot: DataSnapshot) {
                val studentInfo:StudentInfo?=snapshot.getValue(StudentInfo::class.java)
                studentInfo?.id=snapshot.key.toString()
                if (studentInfo!=null){
                    array.remove(studentInfo)
                    studentAdapter?.notifyDataSetChanged()
                }
            }
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        })
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        // Inflate the layout for this fragment
        binding=FragmentStudentBinding.inflate(layoutInflater)
        dialogBinding=CustomDialogBinding.inflate(layoutInflater)
        supabaseClient = (mainActivity.application as MyApplication).supabaseClient
        checkAndRequirePermissions() // see if the permissions granted or not
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        linearLayoutManger=LinearLayoutManager(mainActivity)
        binding.recyclerView.layoutManager=linearLayoutManger
        binding.recyclerView.adapter=studentAdapter
        binding.faBtn.setOnClickListener { dialog() }
    }
//    checking ki permission grant hui ki nhi??/ agr nhi hui then call another function
    private fun checkAndRequirePermissions(){
    if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q){
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.R){
            if (Environment.isExternalStorageManager()){
//                permission granted
            }
            else{
                requestManageExternalStoragePermission()
            }
        }
        else{
            if (ContextCompat.checkSelfPermission(mainActivity,
                    android.Manifest.permission.MANAGE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
                requestManageExternalStoragePermission()
            }
        }
    } else {
        if (ContextCompat.checkSelfPermission(
                mainActivity,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                mainActivity,
                arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                PERMISSION_REQUEST_CODE
            )
        }
    }
}
//    settings mai bejdo allowed krane kai liye
private fun requestManageExternalStoragePermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
        try{
            val intent = Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            startActivityForResult(intent,PERMISSION_REQUEST_CODE)
        }catch (e: ActivityNotFoundException){
            Toast.makeText(mainActivity, "ex-ActivityNotFound", Toast.LENGTH_SHORT).show()
        }
    }
}

    private fun dialog(position: Int =-1){
        dialogBinding = CustomDialogBinding.inflate(layoutInflater)
        val dialog = Dialog(mainActivity).apply {
            setContentView(dialogBinding.root)
            setCancelable(true)
            window?.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
            show()
            }
        if (position > -1){
            dialogBinding.saveBtn.setText("Update")
//            update the item
//            get the old data
            dialogBinding.name.setText(array[position].name)
            dialogBinding.Class.setText(array[position].Class)
            dialogBinding.rollNo.setText(array[position].rollNo)
            Glide.with(this@StudentFragment)
                .load(array[position].imag)
                .centerCrop()
                .into(dialogBinding.imgDialog)
        }
        else{
            dialogBinding.saveBtn.setText("Save")
        }

        dialogBinding.imgDialog.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent,PICK_IMAGE_REQUEST)
        }
        dialogBinding.saveBtn.setOnClickListener {
            if (dialogBinding.name.text.toString().isBlank()){
                dialogBinding.name.error="Enter Student Name"
            }
            if (dialogBinding.rollNo.text.toString().isBlank()){
                dialogBinding.rollNo.error="Enter Student RollNo"
            }
            if (dialogBinding.Class.text.toString().isBlank()) {
                dialogBinding.Class.error = "Enter Student Class"
            }
            else{
                Toast.makeText(mainActivity, " clicked saved", Toast.LENGTH_SHORT).show()
                uploadImageToSupabase(imgUri!!,position)
                dialog.dismiss()
            }
        }
        dialogBinding.cancelBtn.setOnClickListener { dialog.dismiss() }
    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment StudentFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            StudentFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
//    settings sai permission granted honee kai baad ka check
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,grantResults: IntArray){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            PERMISSION_REQUEST_CODE ->{
                if (grantResults.isNotEmpty()&& grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(mainActivity,"allowed", Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(mainActivity, "permission not granted", Toast.LENGTH_SHORT).show()
                }
            }
            MANAGE_EXTERNAL_STORAGE_REQUEST_CODE->{
                if (grantResults.isNotEmpty()&& grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(mainActivity,"access of full Storage granted", Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(mainActivity, "permission not granted", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
//    picture ko click kr ke pr result ara?? ara to supabase mai send kro for public uri
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (resultCode == Activity.RESULT_OK && requestCode == PICK_IMAGE_REQUEST){
        data?.data?.let { uri->
            imgUri=uri
            dialogBinding.imgDialog.setImageURI(uri)
        }
    }
}
//using bucket in coroutine to upload the image into supabase
fun uploadImageToSupabase(uri: Uri,position: Int){
    Toast.makeText(mainActivity, "inUploadImage", Toast.LENGTH_SHORT).show()
    val byteArray = uriToByteArray(mainActivity,uri)
    val fileName ="uploads/${System.currentTimeMillis()}.jpg"//document bna de ga bucket mai of name fileName
    val bucket=supabaseClient.storage.from("studentBucket")//choose your bucket name
    lifecycleScope.launch(Dispatchers.IO){    //use lifecycleScope for coroutine Usage
        try {
//          upload image and handle the response
            bucket.uploadAsFlow(fileName,byteArray).collect{
                    status->
                withContext(Dispatchers.Main){
                    when (status){
                        is UploadStatus.Progress ->{
                            Toast.makeText(mainActivity, "Uploading", Toast.LENGTH_SHORT).show()
                        }
                        is UploadStatus.Success ->{
                            Toast.makeText(mainActivity, "Upload Success", Toast.LENGTH_SHORT).show()
                            val imageUrl = bucket.publicUrl(fileName)
                            val img = dialogBinding.imgDialog
                            publicUrl=imageUrl
                            data(position)
                            Glide.with(mainActivity)
                                .load(imageUrl)
                                .placeholder(R.mipmap.no_image2)
                                .into(img)

                        }
                    }
                }
            }
        }
        catch (e : Exception){
            withContext(Dispatchers.Main){
                Toast.makeText(mainActivity, "ERROR Uploading Image ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

    fun data(position: Int){
        if (position > -1 ){
//            mainActivity.binding.loading.visibility = View.VISIBLE
//            update new values
                val key = array[position].id
                val info = StudentInfo(
                    imag = publicUrl,
                    name = dialogBinding.name.text.toString(),
                    Class = dialogBinding.Class.text.toString(),
                    rollNo = dialogBinding.rollNo.text.toString()
                )
                val update = hashMapOf<String,Any>(
                    "$key" to info)
                dbReference.updateChildren(update)
                Toast.makeText(mainActivity, "update", Toast.LENGTH_SHORT).show()
        }
        else{
            val info = StudentInfo(
                id = "",
                imag = publicUrl,
                name = dialogBinding.name.text.toString(),
                Class = dialogBinding.Class.text.toString(),
                rollNo = dialogBinding.rollNo.text.toString()
            )
            dbReference.push().setValue(info).addOnCompleteListener {
                Toast.makeText(mainActivity, "add", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(mainActivity, "Failed", Toast.LENGTH_SHORT).show()
            }
        }
    }
    fun uriToByteArray(context: Context, uri:Uri):ByteArray{
        val inputStream = context.contentResolver.openInputStream(uri)// content resolver uri ko read krne kaa kaam krta hai
        return inputStream?.readBytes() ?: ByteArray(0)
    }



    override fun Update(position: Int) {
        dialog(position)
    }

    override fun deleteClick(studentInfo: StudentInfo, position: Int) {
        var alertDialog = androidx.appcompat.app.AlertDialog.Builder(mainActivity)
        alertDialog.setTitle("Delete Item")
        alertDialog.setMessage("Do you want to delete the item?")
        alertDialog.setCancelable(false)
        alertDialog.setNegativeButton("No") { _, _ ->
            alertDialog.setCancelable(true)
        }
        alertDialog.setPositiveButton("Yes") { _, _ ->
            if (array.size == 0){
                Toast.makeText(mainActivity, "List Is Empty", Toast.LENGTH_SHORT).show()
            }
            else {
                Toast.makeText(
                    mainActivity,
                    "The item is  deleted",
                    Toast.LENGTH_SHORT
                ).show()
                dbReference.child(studentInfo.id?:"").removeValue()
            }
        }
        alertDialog.show()    }

    override fun itemClicked(position: Int, model: StudentInfo) {
        findNavController().navigate(R.id.action_studentFragment_to_detailFragment,
            bundleOf("name" to model.name,
            "class" to model.Class ,
            "rollNo" to model.rollNo,"img" to model.imag))
        Toast.makeText(mainActivity, "chl rha hai", Toast.LENGTH_SHORT).show()
    }
}