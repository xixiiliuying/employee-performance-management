package com.controller;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Map;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;

import com.entity.NoticeEntity;
import com.entity.view.NoticeView;

import com.service.NoticeService;
import com.utils.PageUtils;
import com.utils.R;
import com.utils.MPUtil;


/**
 * 公告信息
 * 后端接口
 * @author
 * @email
 * @date 2021-05-07 10:42:30
 */
@RestController
@RequestMapping("/notice")
public class NoticeController {
    @Autowired
    private NoticeService noticeService;



    /**
     * 后端列表
     */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params,NoticeEntity notice,
                  HttpServletRequest request){
        EntityWrapper<NoticeEntity> ew = new EntityWrapper<NoticeEntity>();
        PageUtils page = noticeService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, notice), params), params));

        return R.ok().put("data", page);
    }

    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params, NoticeEntity notice,
                  HttpServletRequest request){
        EntityWrapper<NoticeEntity> ew = new EntityWrapper<NoticeEntity>();

        // 按发布日期倒序
        ew.orderBy("publish_date", false);

        // 标题模糊搜索
        if (params.get("title") != null && !params.get("title").toString().trim().isEmpty()) {
            ew.like("title", params.get("title").toString().trim());
        }

        // 日期范围筛选
        if (params.get("startDate") != null && params.get("endDate") != null) {
            ew.between("publish_date", params.get("startDate"), params.get("endDate"));
        }
        PageUtils page = noticeService.queryPage(params, MPUtil.sort(ew, params));
        return R.ok().put("data", page);
    }


    /**
     * 前端列表（带分页）- 专门为前端公告查看优化
     */
    @RequestMapping("/frontList")
    public R frontList(@RequestParam Map<String, Object> params, HttpServletRequest request){
        EntityWrapper<NoticeEntity> ew = new EntityWrapper<NoticeEntity>();

        // 关键词搜索
        if (params.get("keyword") != null && !params.get("keyword").toString().trim().isEmpty()) {
            String keyword = params.get("keyword").toString().trim();
            ew.andNew()
                    .like("title", keyword)
                    .or()
                    .like("content", keyword);
        }

        // 日期范围筛选
        if (params.get("startDate") != null && !params.get("startDate").toString().isEmpty()
                && params.get("endDate") != null && !params.get("endDate").toString().isEmpty()) {
            ew.between("publish_date", params.get("startDate"), params.get("endDate"));
        }

        // 分页参数处理
        if (params.get("page") == null) {
            params.put("page", "1");
        }
        if (params.get("limit") == null) {
            params.put("limit", "10");
        }

        // 在params中添加排序参数，让MPUtil处理排序
        params.put("sort", "publish_date");
        params.put("order", "desc");

        PageUtils page = noticeService.queryPage(params, MPUtil.sort(ew, params));
        return R.ok().put("data", page);
    }
    /**
     * 列表
     */
    @RequestMapping("/lists")
    public R list( NoticeEntity notice){
        EntityWrapper<NoticeEntity> ew = new EntityWrapper<NoticeEntity>();
        ew.allEq(MPUtil.allEQMapPre( notice, "notice"));
        return R.ok().put("data", noticeService.selectListView(ew));
    }

    /**
     * 查询
     */
    @RequestMapping("/query")
    public R query(NoticeEntity notice){
        EntityWrapper< NoticeEntity> ew = new EntityWrapper< NoticeEntity>();
        ew.allEq(MPUtil.allEQMapPre( notice, "notice"));
        NoticeView noticeView =  noticeService.selectView(ew);
        return R.ok("查询公告信息成功").put("data", noticeView);
    }

    /**
     * 后端详情
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
        NoticeEntity notice = noticeService.selectById(id);
        return R.ok().put("data", notice);
    }

    /**
     * 前端详情
     */
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Long id){
        NoticeEntity notice = noticeService.selectById(id);
        return R.ok().put("data", notice);
    }

    /**
     * 后端保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody NoticeEntity notice, HttpServletRequest request){
        notice.setId(new Date().getTime()+new Double(Math.floor(Math.random()*1000)).longValue());

        // 设置默认值
        if (notice.getPublishDate() == null) {
            notice.setPublishDate(new Date());
        }
        if (notice.getCreateTime() == null) {
            notice.setCreateTime(new Date());
        }
        if (notice.getUpdateTime() == null) {
            notice.setUpdateTime(new Date());
        }

        //ValidatorUtils.validateEntity(notice);
        noticeService.insert(notice);
        return R.ok();
    }

    /**
     * 前端保存
     */
    @RequestMapping("/add")
    public R add(@RequestBody NoticeEntity notice, HttpServletRequest request){
        notice.setId(new Date().getTime()+new Double(Math.floor(Math.random()*1000)).longValue());

        // 设置默认值
        if (notice.getPublishDate() == null) {
            notice.setPublishDate(new Date());
        }
        if (notice.getCreateTime() == null) {
            notice.setCreateTime(new Date());
        }
        if (notice.getUpdateTime() == null) {
            notice.setUpdateTime(new Date());
        }

        //ValidatorUtils.validateEntity(notice);
        noticeService.insert(notice);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody NoticeEntity notice, HttpServletRequest request){
        // 更新修改时间
        notice.setUpdateTime(new Date());

        //ValidatorUtils.validateEntity(notice);
        noticeService.updateById(notice);//全部更新
        return R.ok();
    }


    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
        noticeService.deleteBatchIds(Arrays.asList(ids));
        return R.ok();
    }

    /**
     * 提醒接口
     */
    @RequestMapping("/remind/{columnName}/{type}")
    public R remindCount(@PathVariable("columnName") String columnName, HttpServletRequest request,
                         @PathVariable("type") String type,@RequestParam Map<String, Object> map) {
        map.put("column", columnName);
        map.put("type", type);

        if(type.equals("2")) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Calendar c = Calendar.getInstance();
            Date remindStartDate = null;
            Date remindEndDate = null;
            if(map.get("remindstart")!=null) {
                Integer remindStart = Integer.parseInt(map.get("remindstart").toString());
                c.setTime(new Date());
                c.add(Calendar.DAY_OF_MONTH,remindStart);
                remindStartDate = c.getTime();
                map.put("remindstart", sdf.format(remindStartDate));
            }
            if(map.get("remindend")!=null) {
                Integer remindEnd = Integer.parseInt(map.get("remindend").toString());
                c.setTime(new Date());
                c.add(Calendar.DAY_OF_MONTH,remindEnd);
                remindEndDate = c.getTime();
                map.put("remindend", sdf.format(remindEndDate));
            }
        }

        Wrapper<NoticeEntity> wrapper = new EntityWrapper<NoticeEntity>();
        if(map.get("remindstart")!=null) {
            wrapper.ge(columnName, map.get("remindstart"));
        }
        if(map.get("remindend")!=null) {
            wrapper.le(columnName, map.get("remindend"));
        }

        int count = noticeService.selectCount(wrapper);
        return R.ok().put("count", count);
    }

    /**
     * 根据发布日期统计
     */
    @RequestMapping("/statistics")
    public R statistics(@RequestParam Map<String, Object> params) {
        EntityWrapper<NoticeEntity> ew = new EntityWrapper<NoticeEntity>();

        // 可以添加各种统计条件
        if (params.get("startDate") != null) {
            ew.ge("publish_date", params.get("startDate"));
        }
        if (params.get("endDate") != null) {
            ew.le("publish_date", params.get("endDate"));
        }

        int count = noticeService.selectCount(ew);
        return R.ok().put("count", count);
    }

    /**
     * 获取最新公告
     */
    @RequestMapping("/latest")
    public R getLatestNotices(@RequestParam(defaultValue = "5") Integer count) {
        EntityWrapper<NoticeEntity> ew = new EntityWrapper<NoticeEntity>();
        ew.orderBy("publish_date", false);
        ew.last("LIMIT " + count);

        return R.ok().put("data", noticeService.selectList(ew));
    }
}