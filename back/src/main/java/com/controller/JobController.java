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

import com.entity.JobEntity;
import com.entity.view.JobView;

import com.service.JobService;
import com.utils.PageUtils;
import com.utils.R;
import com.utils.MPUtil;


/**
 * 岗位
 * 后端接口
 * @author 
 * @email 
 * @date 2021-05-07 10:42:31
 */
@RestController
@RequestMapping("/job")
public class JobController {
    @Autowired
    private JobService jobService;
    


    /**
     * 后端列表
     */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params,JobEntity gangwei,
		HttpServletRequest request){
        EntityWrapper<JobEntity> ew = new EntityWrapper<JobEntity>();
		PageUtils page = jobService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, gangwei), params), params));

        return R.ok().put("data", page);
    }
    
    /**
     * 前端列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params,JobEntity gangwei, 
		HttpServletRequest request){
        EntityWrapper<JobEntity> ew = new EntityWrapper<JobEntity>();
		PageUtils page = jobService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, gangwei), params), params));
        return R.ok().put("data", page);
    }

	/**
     * 列表
     */
    @RequestMapping("/lists")
    public R list( JobEntity gangwei){
       	EntityWrapper<JobEntity> ew = new EntityWrapper<JobEntity>();
      	ew.allEq(MPUtil.allEQMapPre( gangwei, "gangwei")); 
        return R.ok().put("data", jobService.selectListView(ew));
    }

	 /**
     * 查询
     */
    @RequestMapping("/query")
    public R query(JobEntity gangwei){
        EntityWrapper< JobEntity> ew = new EntityWrapper< JobEntity>();
 		ew.allEq(MPUtil.allEQMapPre( gangwei, "gangwei")); 
		JobView gangweiView =  jobService.selectView(ew);
		return R.ok("查询岗位成功").put("data", gangweiView);
    }
	
    /**
     * 后端详情
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
        JobEntity gangwei = jobService.selectById(id);
        return R.ok().put("data", gangwei);
    }

    /**
     * 前端详情
     */
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Long id){
        JobEntity gangwei = jobService.selectById(id);
        return R.ok().put("data", gangwei);
    }
    



    /**
     * 后端保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody JobEntity gangwei, HttpServletRequest request){
    	gangwei.setId(new Date().getTime()+new Double(Math.floor(Math.random()*1000)).longValue());
    	//ValidatorUtils.validateEntity(gangwei);
        jobService.insert(gangwei);
        return R.ok();
    }
    
    /**
     * 前端保存
     */
    @RequestMapping("/add")
    public R add(@RequestBody JobEntity gangwei, HttpServletRequest request){
    	gangwei.setId(new Date().getTime()+new Double(Math.floor(Math.random()*1000)).longValue());
    	//ValidatorUtils.validateEntity(gangwei);
        jobService.insert(gangwei);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody JobEntity gangwei, HttpServletRequest request){
        //ValidatorUtils.validateEntity(gangwei);
        jobService.updateById(gangwei);//全部更新
        return R.ok();
    }
    

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
        jobService.deleteBatchIds(Arrays.asList(ids));
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
		
		Wrapper<JobEntity> wrapper = new EntityWrapper<JobEntity>();
		if(map.get("remindstart")!=null) {
			wrapper.ge(columnName, map.get("remindstart"));
		}
		if(map.get("remindend")!=null) {
			wrapper.le(columnName, map.get("remindend"));
		}


		int count = jobService.selectCount(wrapper);
		return R.ok().put("count", count);
	}
	


}
