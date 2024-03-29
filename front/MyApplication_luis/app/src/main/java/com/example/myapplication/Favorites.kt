package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.example.dto.Favorite
import com.example.dto.request.FavoriteLocationCoordinateRequest
import com.example.dto.request.TokenRefreshRequest
import com.example.dto.response.ServerResponse
import com.example.dto.response.TokenRefreshResponse
import com.example.util.prefs.App
import com.example.util.retrofit.RetrofitBuilder
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_route_list.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Favorites : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorites)

        val favoriteIdValues = ArrayList<String>()
        val texts = ArrayList<TextView>()
        val layouts = ArrayList<LinearLayout>()
        val deletes = ArrayList<ImageButton>()

        for (i: Int in 1..5) {
            val textId : (String) = "text" + i
            val layoutId : (String) = "layout" + i
            val deleteId : (String) = "delete" + i
            val resId1 = resources.getIdentifier(textId, "id", packageName)
            val resId2 = resources.getIdentifier(layoutId, "id", packageName)
            val resId3 = resources.getIdentifier(deleteId, "id", packageName)
            val tmp1 : (TextView) = findViewById(resId1)
            val tmp2 : (LinearLayout) = findViewById(resId2)
            val tmp3 : (ImageButton) = findViewById(resId3)
            texts.add(tmp1)
            layouts.add(tmp2)
            deletes.add(tmp3)
        }


        /*
            1. GET: 즐겨찾기 조회(https://github.com/legowww/time-to-out/issues/44#issuecomment-1409048102)
         */
        val accessToken = "Bearer ${App.prefs.access}"
        RetrofitBuilder.api.getFavorites(accessToken).enqueue(object : Callback<ServerResponse<List<Favorite>>> {
            override fun onResponse(
                call: Call<ServerResponse<List<Favorite>>>,
                response: Response<ServerResponse<List<Favorite>>>
            ) {
                val body = response.body() ?: return
                val message = body.message
                var count = 0
                if (message.equals("success")) {
                    val myEnrollFavoriteCount = body.result.size //내가 등록한 즐겨찾기 개수

                    //내가 등록한 즐겨찾기가 하나도 없는 경우
                    if (myEnrollFavoriteCount == 0) {
                        println("[info] 내가 등록한 즐겨찾기가 존재하지 않음")
                    }
                    else {
                        val favorites : List<Favorite> = body.result
                        println("내 즐겨찾기 개수=$myEnrollFavoriteCount")
                        //출력 확인
                        for (favorite in favorites) {
                            favoriteIdValues.add(count, favorite.id)
                            texts[count].text = favorite.name
                            layouts[count].visibility = View.VISIBLE
                            texts[count].setOnClickListener {
                                val subIntent = Intent(this@Favorites, Routelist::class.java)
                                subIntent.putExtra("sx", favorite.lc.sx)
                                subIntent.putExtra("sy", favorite.lc.sy)
                                subIntent.putExtra("ex", favorite.lc.ex)
                                subIntent.putExtra("ey", favorite.lc.ey)
                                subIntent.putExtra("identify", 0)
                                startActivity(subIntent)
                            }
                            count++
                            println("[info] ====== id=${favorite.id} name=${favorite.name}, lc=${favorite.lc} ======")
                        }
                    }
                }
                else {
                    RetrofitBuilder.api.refresh(TokenRefreshRequest(App.prefs.refresh)).enqueue(object : Callback<ServerResponse<TokenRefreshResponse>> {
                        override fun onResponse(
                            call: Call<ServerResponse<TokenRefreshResponse>>,
                            response: Response<ServerResponse<TokenRefreshResponse>>
                        ) {
                            val body = response.body() ?: return
                            val message = body.message
                            if (message.equals("success")) {
                                //[성공] -> 현재 화면 다시 요청
                                val newAccessToken = body.result.access
                                App.prefs.access = newAccessToken
                                val intent = Intent(this@Favorites, Favorites::class.java)
                                startActivity(intent)
                            }
                            else {
                                //[실급]실패 -> 로그인 화면
                                Toast.makeText(this@Favorites, "세션 종료. 로그인", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this@Favorites, Login::class.java)
                                startActivity(intent)
                            }
                        }
                        override fun onFailure(
                            call: Call<ServerResponse<TokenRefreshResponse>>,
                            t: Throwable
                        ) {
                        }
                    })
                }
            }
            override fun onFailure(call: Call<ServerResponse<List<Favorite>>>, t: Throwable) {
            }
        })

        for (i: Int in 0 .. 4) {
            deletes[i].setOnClickListener {
                val accessToken = "Bearer ${App.prefs.access}"

                RetrofitBuilder.api.deleteFavorite(accessToken, favoriteIdValues[i]).enqueue(object : Callback<ServerResponse<String>> {
                    override fun onResponse(
                        call: Call<ServerResponse<String>>,
                        response: Response<ServerResponse<String>>
                    ) {
                        val body = response.body() ?: return
                        val message = body.message
                        if (message.equals("success")) {
                            Toast.makeText(this@Favorites, "즐겨찾기 삭제", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onFailure(call: Call<ServerResponse<String>>, t: Throwable) {
                    }
                })
                texts[i].text = ""
                layouts[i].visibility = View.GONE
            }
        }

        //val back = Intent(this,Routelist::class.java)
        val star = Intent(this,Favorites::class.java)
        val home = Intent(this, MainActivity::class.java)
        //val reload = Intent(this, Detail::class.java)
        val account = Intent(this, Mypage::class.java)

        val tabs : (TabLayout) = findViewById(R.id.tabs)
        tabs.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab!!.position) {
                    0 -> startActivity(home)
                    1 -> startActivity(star)
                    2 -> startActivity(home)
                    3 -> startActivity(star)
                    4 -> startActivity(account)
                }
            }
        })
    }
}