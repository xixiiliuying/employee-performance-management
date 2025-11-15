package com.service;

import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.service.IService;
import com.utils.PageUtils;
import com.entity.NoticeEntity;
import java.util.List;
import java.util.Map;
import com.entity.vo.NoticeVO;
import org.apache.ibatis.annotations.Param;
import com.entity.view.NoticeView;


/**
 * 公告信息
 *
 * @author 
 * @email 
 * @date 2021-05-07 10:42:30
 */
public interface NoticeService extends IService<NoticeEntity> {

    PageUtils queryPage(Map<String, Object> params);
    
   	List<NoticeVO> selectListVO(Wrapper<NoticeEntity> wrapper);
   	
   	NoticeVO selectVO(@Param("ew") Wrapper<NoticeEntity> wrapper);
   	
   	List<NoticeView> selectListView(Wrapper<NoticeEntity> wrapper);
   	
   	NoticeView selectView(@Param("ew") Wrapper<NoticeEntity> wrapper);
   	
   	PageUtils queryPage(Map<String, Object> params,Wrapper<NoticeEntity> wrapper);
   	
}

