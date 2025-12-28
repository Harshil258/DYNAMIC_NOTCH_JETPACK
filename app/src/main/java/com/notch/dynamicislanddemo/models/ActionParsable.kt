package com.notch.dynamicislanddemo.models

import android.app.PendingIntent
import android.app.RemoteInput
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils

/**
 * Model class for parsed notification actions
 */
data class ActionParsable(
    val title: CharSequence?,
    val actionIntent: PendingIntent?,
    val extras: Bundle?,
    val remoteInputs: Array<RemoteInput>?,
    val allowGeneratedReplies: Boolean,
    val semanticAction: Int,
    val isContextual: Boolean,
    val icon: Int
) : Parcelable {
    constructor(parcel: Parcel) : this(
        TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(parcel),
        parcel.readParcelable(PendingIntent::class.java.classLoader),
        parcel.readBundle(Bundle::class.java.classLoader),
        parcel.createTypedArray(RemoteInput.CREATOR),
        parcel.readInt() != 0,
        parcel.readInt(),
        parcel.readInt() != 0,
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        TextUtils.writeToParcel(title, parcel, flags)
        parcel.writeParcelable(actionIntent, flags)
        parcel.writeBundle(extras)
        parcel.writeTypedArray(remoteInputs, flags)
        parcel.writeInt(if (allowGeneratedReplies) 1 else 0)
        parcel.writeInt(semanticAction)
        parcel.writeInt(if (isContextual) 1 else 0)
        parcel.writeInt(icon)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ActionParsable> {
        override fun createFromParcel(parcel: Parcel): ActionParsable {
            return ActionParsable(parcel)
        }

        override fun newArray(size: Int): Array<ActionParsable?> {
            return arrayOfNulls(size)
        }
    }
}
