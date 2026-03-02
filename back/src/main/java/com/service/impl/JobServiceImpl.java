package com.service.impl;

import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.List;

import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.utils.PageUtils;
import com.utils.Query;


import com.dao.JobDao;
import com.entity.JobEntity;
import com.service.JobService;
import com.entity.vo.JobVO;
import com.entity.view.JobView;

@Service("gangweiService")
public class JobServiceImpl extends ServiceImpl<JobDao, JobEntity> implements JobService {
	
	
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        Page<JobEntity> page = this.selectPage(
                new Query<JobEntity>(params).getPage(),
                new EntityWrapper<JobEntity>()
        );
        return new PageUtils(page);
    }
    
    @Override
	public PageUtils queryPage(Map<String, Object> params, Wrapper<JobEntity> wrapper) {
		  Page<JobView> page =new Query<JobView>(params).getPage();
	        page.setRecords(baseMapper.selectListView(page,wrapper));
	    	PageUtils pageUtil = new PageUtils(page);
	    	return pageUtil;
 	}
    
    @Override
	public List<JobVO> selectListVO(Wrapper<JobEntity> wrapper) {
 		return baseMapper.selectListVO(wrapper);
	}
	
	@Override
	public JobVO selectVO(Wrapper<JobEntity> wrapper) {
 		return baseMapper.selectVO(wrapper);
	}
	
	@Override
	public List<JobView> selectListView(Wrapper<JobEntity> wrapper) {
		return baseMapper.selectListView(wrapper);
	}

	@Override
	public JobView selectView(Wrapper<JobEntity> wrapper) {
		return baseMapper.selectView(wrapper);
	}

}
