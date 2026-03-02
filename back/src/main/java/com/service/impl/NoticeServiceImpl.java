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

import com.dao.NoticeDao;
import com.entity.NoticeEntity;
import com.service.NoticeService;
import com.entity.vo.NoticeVO;
import com.entity.view.NoticeView;

@Service("noticeService")
public class NoticeServiceImpl extends ServiceImpl<NoticeDao, NoticeEntity> implements NoticeService {

	/**
	 * 默认分页查询（不带条件）
	 */
	@Override
	public PageUtils queryPage(Map<String, Object> params) {
		// 默认分页查询，按发布时间倒序
		Page<NoticeEntity> page = this.selectPage(
				new Query<NoticeEntity>(params).getPage(),
				new EntityWrapper<NoticeEntity>().orderBy("publish_date", false)
		);
		return new PageUtils(page);
	}

	/**
	 * 条件分页查询（用于前端带查询条件、分页的接口）
	 */
	@Override
	public PageUtils queryPage(Map<String, Object> params, Wrapper<NoticeEntity> wrapper) {
		Page<NoticeView> page = new Query<NoticeView>(params).getPage();

		// ✅ 添加排序逻辑（不再由 XML 决定，防止重复 ORDER BY）
		if (wrapper == null) {
			wrapper = new EntityWrapper<NoticeEntity>().orderBy("n.publish_date", false);
		} else {
			((EntityWrapper<NoticeEntity>) wrapper).orderBy("n.publish_date", false);
		}

		page.setRecords(baseMapper.selectListView(page, wrapper));
		return new PageUtils(page);
	}

	@Override
	public List<NoticeVO> selectListVO(Wrapper<NoticeEntity> wrapper) {
		return baseMapper.selectListVO(wrapper);
	}

	@Override
	public NoticeVO selectVO(Wrapper<NoticeEntity> wrapper) {
		return baseMapper.selectVO(wrapper);
	}

	@Override
	public List<NoticeView> selectListView(Wrapper<NoticeEntity> wrapper) {
		return baseMapper.selectListView(wrapper);
	}

	@Override
	public NoticeView selectView(Wrapper<NoticeEntity> wrapper) {
		return baseMapper.selectView(wrapper);
	}
}
