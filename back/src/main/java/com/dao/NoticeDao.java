package com.dao;

import com.entity.NoticeEntity;
import com.baomidou.mybatisplus.mapper.BaseMapper;
import java.util.List;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.plugins.pagination.Pagination;

import org.apache.ibatis.annotations.Param;
import com.entity.vo.NoticeVO;
import com.entity.view.NoticeView;


/**
 * 公告信息
 *
 * @author
 * @email
 * @date 2021-05-07 10:42:30
 */
public interface NoticeDao extends BaseMapper<NoticeEntity> {

	List<NoticeVO> selectListVO(@Param("ew") Wrapper<NoticeEntity> wrapper);

	NoticeVO selectVO(@Param("ew") Wrapper<NoticeEntity> wrapper);

	List<NoticeView> selectListView(@Param("ew") Wrapper<NoticeEntity> wrapper);

	List<NoticeView> selectListView(Pagination page, @Param("ew") Wrapper<NoticeEntity> wrapper);

	NoticeView selectView(@Param("ew") Wrapper<NoticeEntity> wrapper);

}