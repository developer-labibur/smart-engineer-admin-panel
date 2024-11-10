package com.rudhashi.seadminpanel.util

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.rudhashi.seadminpanel.model.Department
import com.rudhashi.seadminpanel.model.Semester

class MakeFirebaseEasy {

    companion object {
        fun getAllDepartment(allDepartment: ArrayList<String>): ArrayList<String> {
            FirebaseFirestore.getInstance().collection ("departments").get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val xDep = document.toObject(Department::class.java)
                        val dName = xDep.DName
                        val dCode = xDep.DCode
                        val department = "$dCode => $dName"
                        allDepartment.add(department)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("MakeFirebaseEasy", "Error loading units: ", exception)
                }
            return allDepartment
        }
        fun getAllSemester(allDepartment: ArrayList<String>): ArrayList<String> {
            FirebaseFirestore.getInstance().collection ("semester").get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val semester = document.toObject(Semester::class.java)
                        allDepartment.add(semester.sem)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("MakeFirebaseEasy", "Error loading units: ", exception)
                }
            return allDepartment
        }

    }
}