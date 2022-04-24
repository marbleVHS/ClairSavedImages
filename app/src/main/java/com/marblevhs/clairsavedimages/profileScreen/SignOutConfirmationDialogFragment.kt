package com.marblevhs.clairsavedimages.profileScreen

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.marblevhs.clairsavedimages.MainActivity

class SignOutConfirmationDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Are you sure?")
            .setPositiveButton("Sign out") { _, _ ->
                (activity as MainActivity).clearAccessToken()
            }
            .setNegativeButton("Cancel") { _, _ ->

            }
            .create()

    companion object {
        const val TAG = "SignOutConfirmationDialog"
    }

}