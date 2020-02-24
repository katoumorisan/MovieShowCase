package com.yzl.movieshowcase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.yzl.movieshowcase.adapter.ItemAdapter;
import com.yzl.movieshowcase.api.ApiClient;
import com.yzl.movieshowcase.model.Item;
import com.yzl.movieshowcase.model.SearchItem;
import com.yzl.searchbox.SearchFragment;
import com.yzl.searchbox.custom.IOnSearchClickListener;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.PermissionRequest;

public class MainActivity extends AppCompatActivity implements ItemAdapter.OnItemListener
        , Toolbar.OnMenuItemClickListener, IOnSearchClickListener {
    //api密钥
    private static final String API_KEY = "19b0bce5";
    private static final int PERMISSION_REQUEST = 11;
    //刷新控件
    private SwipeRefreshLayout swipeRefreshLayout;
    //recyclerview
    private RecyclerView recyclerView;
    //获取的list集合
    private List<Item> ItemList = new ArrayList<>();
    //listitem适配器
    private ItemAdapter itemAdapter;
    //当前页码
    private int pageNum = 1;
    //总页码
    private int totalPagelNum=0;
    //总页数List
    private List<String> totalPageNumList = new ArrayList<>();
    //总页数Str
    private String[] totalPageNumStr = {};
    //搜索控件
    private SearchFragment searchFragment;
    //toolbar
    private Toolbar toolbar;
    //关键字
    private String keyWord;
    //disposable
    protected Disposable disposable;
    //悬浮按钮
    private FloatingActionButton fab;
    //搜索空图片
    private ImageView emptyIv;
    //弹窗提示框
    private AlertDialog dialog;

    //说明：
    //加了一个自己组件化的插件searchbox，有搜索和搜索历史，界面也整挺美
    //图片加载用的是Rxjava+Retrofit+Picasso
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //初始化主界面
        setContentView(R.layout.activity_main);
        initView();
        methodRequiresPermissions();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @AfterPermissionGranted(PERMISSION_REQUEST)
    private void methodRequiresPermissions() {
        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS
        ,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.ACCESS_NETWORK_STATE
        ,Manifest.permission.CHANGE_NETWORK_STATE,Manifest.permission.INTERNET};
        if (EasyPermissions.hasPermissions(this, perms)) {
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(
                    new PermissionRequest.Builder(this, PERMISSION_REQUEST, perms)
                            .setRationale("是否允许获取网络、数据读写权限？")
                            .setPositiveButtonText("允许")
                            .setNegativeButtonText("不允许")
                            .build());
        }
    }

    /**
     * 初始化view
     */
    private void initView(){
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("MovieSearch");//标题
        setSupportActionBar(toolbar);
        //搜索控件
        searchFragment = SearchFragment.newInstance();
        toolbar.setOnMenuItemClickListener(this);
        searchFragment.setOnSearchClickListener(this);
        recyclerView = findViewById(R.id.item_recyclerview);
        final LinearLayoutManager manger = new LinearLayoutManager(MainActivity.this);
        recyclerView.setLayoutManager(manger);
        itemAdapter = new ItemAdapter(ItemList, MainActivity.this);
        recyclerView.setAdapter(itemAdapter);
        //设置recyclerview的onscrollLister来控制滑动到底部的触发事件
        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            int lastVisibleItem=0;
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView,
                                             int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                //滑动到底端
                if (newState == RecyclerView.SCROLL_STATE_IDLE
                        && lastVisibleItem + 1 == itemAdapter.getItemCount()) {
                    if(pageNum==totalPagelNum){
                        Toast.makeText(MainActivity.this,"到底啦～",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    final Snackbar snackbar = Snackbar.make(fab, "Enter next page?", Snackbar
                            .LENGTH_LONG);
                    snackbar.setActionTextColor(getResources().getColor(R.color.white));
                    snackbar.setAction("YES", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //搜索下一页
                            onSearch(API_KEY,String.valueOf(++pageNum),keyWord);
                            snackbar.dismiss();
                        }
                    });
                    snackbar.show();
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                //获取当前屏幕可见的最下方item的index
                lastVisibleItem = manger.findLastVisibleItemPosition();
            }

        });
        swipeRefreshLayout = findViewById(R.id.item_swiperefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //判断是否key值不可用
                if(null!=keyWord && !keyWord.isEmpty()) {
                    //搜索
                    onSearch(API_KEY,String.valueOf(pageNum),keyWord);
                }else{
                    swipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(MainActivity.this,"请输入搜索值",Toast.LENGTH_SHORT).show();
                }
            }
        });
        //悬浮按钮，用来跳转页面
        fab=findViewById(R.id.more_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog = getPageDialog();
                dialog.show();
            }
        });
        emptyIv=findViewById(R.id.empty_iv);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //加载菜单文件
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                //点击搜索
                searchFragment.show(getSupportFragmentManager(), SearchFragment.TAG);
                break;
        }
        return true;
    }

    @Override
    public void OnSearchClick(String keyword) {
        keyWord = keyword;
        swipeRefreshLayout.setRefreshing(true);
        pageNum=1;
        onSearch(API_KEY,String.valueOf(pageNum),keyWord);
        toolbar.setTitle("MovieSearch:"+keyword);
    }

    /**
     * 搜索方法
     * @param apikey apikey
     * @param page 页数
     * @param keyWord 关键字
     */
    private void onSearch(String apikey, final String page, String keyWord){
        disposable = ApiClient.getApiInterface()
                .getSearchItems(apikey,keyWord,page)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<SearchItem>() {
                    @Override
                    public void accept(SearchItem searchItem) throws Exception {
                        //获取结果成功
                        totalPagelNum = mod(searchItem.getTotalResults(),10);
                        //若搜索为空或搜索返回为false
                        if(totalPagelNum==0 || !searchItem.isResponse()){
                            ItemList = new ArrayList<>();
                            itemAdapter = new ItemAdapter(ItemList, MainActivity.this);
                            recyclerView.setAdapter(itemAdapter);
                            itemAdapter.notifyDataSetChanged();
                            swipeRefreshLayout.setRefreshing(false);
                            emptyIv.setVisibility(View.VISIBLE);
                            return;
                        }
                        emptyIv.setVisibility(View.GONE);
                        totalPageNumIntToList();
                        ItemList = searchItem.getSearch();
                        itemAdapter = new ItemAdapter(ItemList, MainActivity.this);
                        recyclerView.setAdapter(itemAdapter);
                        itemAdapter.notifyDataSetChanged();
                        swipeRefreshLayout.setRefreshing(false);
                        Toast.makeText(MainActivity.this,"第"+page+"页",Toast.LENGTH_SHORT).show();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        //若发生异常，reset界面
                        ItemList.clear();
                        totalPageNumStr = new String[]{};
                        dialog=null;
                        itemAdapter.notifyDataSetChanged();
                        emptyIv.setVisibility(View.VISIBLE);
                        swipeRefreshLayout.setRefreshing(false);
                        Toast.makeText(MainActivity.this,R.string.search_error,Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * 获取页数
     * @param a item总数
     * @param b 单页条目数
     * @return 总页数
     */
    public int mod(int a, int b){
        int c = 0;
        if(a==0){
            return c;
        }else if(a>0) {
            c = a % b==0?a/b:a/b+1;
        }
        return c;
    }

    /**
     * 终止订阅
     */
    protected void unsubscribe() {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }

    /**
     * 获取页数dialog
     * @return dialog
     */
    private AlertDialog getPageDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("请选择页数");
        builder.setItems(totalPageNumStr, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                pageNum=Integer.valueOf(totalPageNumStr[which]);
                onSearch(API_KEY,totalPageNumStr[which],keyWord);
//                Toast.makeText(MainActivity.this,"第"+totalPageNumStr[which]+"页",Toast.LENGTH_SHORT).show();
            }
        });
        AlertDialog alertDialog = builder.create();
        return alertDialog;
    }

    /**
     * 总页数int转List，再赋值dialog的String【】
     */
    private void totalPageNumIntToList(){
        totalPageNumList.clear();
        for(int i=0;i<totalPagelNum;i++){
            totalPageNumList.add((i+1)+"");
        }
        totalPageNumStr = totalPageNumList.toArray(new String[totalPageNumList.size()]);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //解除订阅
        unsubscribe();
    }

    @Override
    public void OnItemClick(int position) {
        //点击item跳转至详情界面

    }
}
