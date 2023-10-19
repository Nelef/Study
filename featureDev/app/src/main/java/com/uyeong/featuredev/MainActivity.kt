package com.uyeong.featuredev

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.uyeong.featuredev.ui.theme.FeatureDevTheme
import ezvcard.Ezvcard
import ezvcard.VCard
import ezvcard.property.FormattedName
import ezvcard.property.Organization
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream


class MainActivity : ComponentActivity() {
    private val PERMISSION_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            FeatureDevTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TestUI {
                        if (hasContactsPermission()) {
                            exportContactsToVcf()
                        } else {
                            requestContactsPermission()
                        }
                    }
                }
            }
        }
    }

    private fun hasContactsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestContactsPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_CONTACTS),
            PERMISSION_REQUEST_CODE
        )
    }

    // 주소록을 VCF로 저장하고 읽는 함수
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            exportContactsToVcf()
        } else {
            Toast.makeText(this, "권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun exportContactsToVcf() {
        val contacts = fetchContacts()
        val vCards = contacts.map { contactToVCard(it) }

        val vcfString = Ezvcard.write(vCards).go()

        val file = File(getExternalFilesDir(null), "contacts.vcf")
        FileOutputStream(file).use {
            it.write(vcfString.toByteArray())
        }

        Toast.makeText(this, "VCF 저장 완료: ${file.path}", Toast.LENGTH_LONG).show()
        Log.i("VCF_Save", "VCF 저장 완료: ${file.path}")

        // Json 으로 변환
        Log.i("VCF_json", Json.encodeToString(contacts))
    }

    @SuppressLint("Range")
    private fun fetchContacts(): List<Contact> {
        val contacts = mutableListOf<Contact>()

        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            null,
            null,
            null
        )

        cursor?.use {
            while (it.moveToNext()) {
                val contactId = it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID))

                val name = it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                val phoneNumber = it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))

                val orgCursor = contentResolver.query(
                    ContactsContract.Data.CONTENT_URI,
                    null,
                    ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?",
                    arrayOf(contactId, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE),
                    null
                )

                var orgName: String? = null
                var title: String? = null

                orgCursor?.use { orgIt ->
                    if (orgIt.moveToFirst()) {
                        orgName = orgIt.getString(orgIt.getColumnIndex(ContactsContract.CommonDataKinds.Organization.COMPANY))
                        title = orgIt.getString(orgIt.getColumnIndex(ContactsContract.CommonDataKinds.Organization.TITLE))
                    }
                }
                orgCursor?.close()

                contacts.add(Contact(name, phoneNumber, orgName, title))
            }
        }
        cursor?.close()

        return contacts
    }

    private fun contactToVCard(contact: Contact): VCard {
        val vcard = VCard()
        vcard.formattedName = FormattedName(contact.name)
        vcard.addTelephoneNumber(contact.phoneNumber)

        // 회사 정보와 직위 추가
        contact.org?.let {
            val org = Organization()
            org.values.add(it)
            vcard.organization = org
        }
        contact.title?.let {
            vcard.addTitle(it)
        }

        return vcard
    }

    @Serializable
    data class Contact(val name: String, val phoneNumber: String, val org: String?, val title: String?)
}

@Composable
fun TestUI(onClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(onClick = { onClick() }) {
            Text(text = "주소록 획득")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun Preview() {
    FeatureDevTheme {
        TestUI {}
    }
}