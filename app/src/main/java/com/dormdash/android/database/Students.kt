package com.dormdash.android.database

class Students {
    var studentID: String? = null
    var firstName: String? = null
    var lastName: String? = null
    var phoneNum: String? = null
    var email: String? = null
    var password: String? = null
    var uid: String? = null

    constructor(){}

    constructor(
        studentID: String,
        firstName: String,
        lastName: String,
        phoneNum: String,
        email: String,
        password: String,
        uid : String
    ){
        this.studentID = studentID
        this.firstName = firstName
        this.lastName = lastName
        this.phoneNum = phoneNum
        this.email = email
        this.password = password
        this.uid = uid
    }
}
