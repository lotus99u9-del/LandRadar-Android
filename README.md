# LandRadar Android

Android client for LandRadar — แอปมือถือแบบเบาสำหรับค้นหาและติดตามทรัพย์

## Mobile scope

- ค้นหาและกรองทรัพย์ขั้นพื้นฐาน
- แผนที่ขนาดเล็กพร้อมจำนวนหมุด
- รายละเอียดทรัพย์แบบย่อ
- บันทึก/เลิกบันทึกทรัพย์
- หน้าการแจ้งเตือนที่จำเป็น
- ข้อมูลวิเคราะห์เชิงลึกเปิดดูต่อบนเว็บไซต์หลัก

## Current state

มี Kotlin + Jetpack Compose prototype ที่กดดู flow ได้ พร้อมข้อมูลตัวอย่างซึ่งแยกผ่าน `PropertyRepository` เพื่อเปลี่ยนไปใช้ API จริงภายหลัง

Authentication มีหน้ารับ identifier และ OTP สองขั้นตอน แต่ยังเป็น demo mode: ไม่ส่ง OTP, ไม่สร้าง session และไม่เชื่อม production backend จนกว่าจะเลือกผู้ให้บริการ Auth และ API

ดู [Authentication architecture](docs/AUTHENTICATION.md)

## Local setup

ต้องใช้ Android Studio, JDK 17, Android SDK 35 และ Gradle ที่รองรับ Android Gradle Plugin 8.7.3

1. Clone repository
2. เปิด root folder ใน Android Studio
3. Sync Gradle
4. รัน configuration `app` บน Android 8.0 ขึ้นไป

ห้าม commit `local.properties`, keystore, environment file, credentials หรือ tokens
