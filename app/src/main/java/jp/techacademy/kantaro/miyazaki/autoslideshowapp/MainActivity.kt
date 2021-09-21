package jp.techacademy.kantaro.miyazaki.autoslideshowapp

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.provider.MediaStore
import android.content.ContentUris
import android.os.Handler
import androidx.core.net.toUri
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    private val PERMISSIONS_REQUEST_CODE = 101

    var imageList :ArrayList<String> = arrayListOf<String>()
    var listSize : Int = 0
    var currentPos : Int = 0

    var mTimer: Timer? = null
    var mHandler = Handler()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getContentsInfo()
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_CODE)
            }
            // Android 5系以下の場合
        } else {
            getContentsInfo()
        }

        forward_button.setOnClickListener {
            if (mTimer == null && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                if (this.currentPos+1 < this.listSize){
                    this.currentPos += 1
                    imageView.setImageURI(this.imageList[this.currentPos].toUri())
                }else{
                    this.currentPos = 0
                    imageView.setImageURI(this.imageList[this.currentPos].toUri())
                }


            }else{}
        }

        back_button.setOnClickListener {
            if (mTimer == null && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                if (this.currentPos > 0){
                    this.currentPos -= 1
                    imageView.setImageURI(this.imageList[this.currentPos].toUri())
                }else{
                    this.currentPos = this.listSize - 1
                    imageView.setImageURI(this.imageList[this.currentPos].toUri())
                }


            }else{}
        }

        start_button.setOnClickListener {
            if (mTimer == null && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                mTimer = Timer()
                start_button.text = "停止"
                mTimer!!.schedule(object : TimerTask() {
                    override fun run() {
                        if (currentPos+1 < listSize){
                            currentPos += 1
                        }else{
                            currentPos = 0
                        }
                        mHandler.post {
                            imageView.setImageURI(imageList[currentPos].toUri())
                        }
                    }
                },2000,2000)

            }
            else if(mTimer != null){
                mTimer!!.cancel()
                mTimer = null
                start_button.text = "再生"
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo()
                }
        }
    }

    private fun getContentsInfo() {
        // 画像の情報を取得する
        val resolver = contentResolver
        val cursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
                null, // 項目（null = 全項目）
                null, // フィルタ条件（null = フィルタなし）
                null, // フィルタ用パラメータ
                null // ソート (nullソートなし）
        )

        if (cursor!!.moveToFirst()) {
            do {
                // indexからIDを取得し、そのIDから画像のURIを取得する
                val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                val id = cursor.getLong(fieldIndex)
                val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

                Log.d("imageUrl", "URI : " + imageUri.toString())
                imageList.add(imageUri.toString())
            } while (cursor.moveToNext())
        }

        imageView.setImageURI(this.imageList[0].toUri())
        this.listSize = imageList.size
        cursor.close()
    }


}