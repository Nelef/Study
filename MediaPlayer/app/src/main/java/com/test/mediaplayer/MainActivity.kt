package com.test.mediaplayer

import android.content.Context
import android.graphics.Color
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.SystemClock
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.SeekBar
import androidx.core.app.ActivityCompat
import com.test.mediaplayer.databinding.ActivityMainBinding
import java.io.File
import java.text.SimpleDateFormat

class MainActivity : AppCompatActivity() {

    lateinit var mp3List: ArrayList<String>    //mp3파일을 저장할 리스트
    lateinit var selectedMp3: String //현재 선택된 mp3파일

    lateinit var mPlayer: MediaPlayer    //mp3 player 객체 생성

    private lateinit var binding: ActivityMainBinding

    /*3. 음악 파일을 SD카드에서 가져오는 경우*/
    /*sd카드 경로 구하기*/
    var mp3Path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path + "/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        title = "13장 수시고사"

        /*처음에 어플 키면 '접근 권한을 허용할까요?' 하는 메시지를 띄움*/
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
            Context.MODE_PRIVATE
        )

        mp3List = ArrayList()   //mp3파일을 저장할 리스트

        var listFiles = File(mp3Path).listFiles()   //해당 경로에 있는 모든 파일들을 File[] 타입 변수에 저장 (mp3말고 다른 파일이 있을 수 있음)
        var fileName: String    //파일 전체 이름
        var extName: String     //확장자 이름
        for (file in listFiles!!) {
            fileName = file.name
            extName = fileName.substring(fileName.length - 3)   //확장자 추출하기
            if (extName == "mp3")   //확장자가 mp3일 경우 List에 추가
                mp3List.add(fileName)
        }

        /*어댑터(내용물 or 컨텐츠의 의미) 생성*/
        var ad = ArrayAdapter(this, android.R.layout.simple_list_item_single_choice, mp3List)

        binding.apply {
            /*리스트뷰에 생성한 어댑터 지정*/
            listViewMp3.choiceMode = ListView.CHOICE_MODE_SINGLE
            listViewMp3.adapter = ad
            listViewMp3.setItemChecked(0, true)  //초기 선택값 : 인덱스 0번에 위치

            /*아이템이 선택되면 selectedMp3에 값을 넣어줌*/
            listViewMp3.setOnItemClickListener { parent, view, position, id ->  //선택한 아이템의 인덱스 값은 position으로 알 수 있음
                selectedMp3 = mp3List[position]
            }
            selectedMp3 = mp3List[0]        //초기값은 0번째 아이템

            mPlayer = MediaPlayer()

            btnPlay.setOnClickListener {
                mPlayer.setDataSource(mp3Path + selectedMp3)    //경로+파일명
                mPlayer.prepare()
                mPlayer.start()     //음악 재생
                mPlayer.isLooping = false   //반복 재생x
                btnPlay.setTextColor(Color.WHITE)   //실행하면 버튼색이 바뀜
                btnStop.setTextColor(Color.RED)
                textView.text = "실행중인 음악 : $selectedMp3"

                /* 실시간으로 변경되는 진행시간과 시크바를 구현하기 위한 스레드 사용*/
                object : Thread() {
                    var timeFormat = SimpleDateFormat("mm:ss")  //"분:초"를 나타낼 수 있도록 포멧팅
                    override fun run() {
                        super.run()
                        if (mPlayer == null)
                            return
                        seekBar.max = mPlayer.duration  // mPlayer.duration : 음악 총 시간
                        while (mPlayer.isPlaying) {
                            runOnUiThread { //화면의 위젯을 변경할 때 사용 (이 메소드 없이 아래 코드를 추가하면 실행x)
                                seekBar.progress = mPlayer.currentPosition
                                textView2.text = "진행시간 : " + timeFormat.format(mPlayer.currentPosition)
                            }
                            SystemClock.sleep(200)
                        }

                        /*1. 음악이 종료되면 자동으로 초기상태로 전환*/
                        /*btnStop.setOnClickListener()와 동일한 코드*/
                        if(!mPlayer.isPlaying){
                            mPlayer.stop()      //음악 정지
                            mPlayer.reset()
                            btnPlay.setTextColor(Color.RED)   //실행하면 버튼색이 바뀜
                            btnStop.setTextColor(Color.WHITE)
                            textView.text = "실행중인 음악 : "
                            seekBar.progress = 0
                            textView2.text = "진행시간 : "
                        }
                    }
                }.start()

            }

            btnStop.setOnClickListener {
                mPlayer.stop()      //음악 정지
                mPlayer.reset()
                btnPlay.setTextColor(Color.RED)   //실행하면 버튼색이 바뀜
                btnStop.setTextColor(Color.WHITE)
                textView.text = "실행중인 음악 : "
                seekBar.progress = 0
                textView2.text = "진행시간 : "

            }

            /*2. 시크바로 음악의 해당 부분을 재생*/
            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser){
                        mPlayer.seekTo(progress)
                    }
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }
    }
}