package com.uyeong.featuredev

import android.content.Context
import android.provider.ContactsContract
import android.util.Log
import android.widget.Toast
import ezvcard.Ezvcard
import ezvcard.VCard
import ezvcard.property.FormattedName
import ezvcard.property.Organization
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream

class ContactsExtraction(private val context: Context) {
    // 주소록을 VCF로 저장
    fun exportContactsToVcf(): List<Contact> {
        val contacts = fetchContacts()

        // Json 으로 변환
        Log.i("VCF_json", Json.encodeToString(contacts))

//        // vcf 로 변환 및 저장
//        val vCards = contacts.map { contactToVCard(it) }
//        val vcfString = Ezvcard.write(vCards).go()
//        val file = File(context.getExternalFilesDir(null), "contacts.vcf")
//        FileOutputStream(file).use {
//            it.write(vcfString.toByteArray())
//        }
//
//        Log.i("VCF_Save", "VCF 저장 완료: ${file.path}")

        Toast.makeText(context, "주소록 추출 및 json 전송 완료", Toast.LENGTH_LONG).show()

        return contacts
    }

    private fun fetchContacts(): List<Contact> {
        val contacts = mutableListOf<Contact>()

        val cursor = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null
        )

        cursor?.use {
            while (it.moveToNext()) {
                val contactId =
                    it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.CONTACT_ID))
                val name =
                    it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                val phoneNumber =
                    it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))

                val orgCursor = context.contentResolver.query(
                    ContactsContract.Data.CONTENT_URI,
                    null,
                    ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?",
                    arrayOf(
                        contactId, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE
                    ),
                    null
                )

                var orgName: String? = null
                var title: String? = null

                orgCursor?.use { orgIt ->
                    if (orgIt.moveToFirst()) {
                        orgName =
                            orgIt.getString(orgIt.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Organization.COMPANY))
                        title =
                            orgIt.getString(orgIt.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Organization.TITLE))
                    }
                }

                contacts.add(Contact(name, phoneNumber, orgName, title))
            }
        }


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
    data class Contact(
        val name: String, val phoneNumber: String, val org: String?, val title: String?
    )
}