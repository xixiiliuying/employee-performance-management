package com.entity.view;

import com.entity.NoticeEntity;

import com.baomidou.mybatisplus.annotations.TableName;
import org.apache.commons.beanutils.BeanUtils;
import java.lang.reflect.InvocationTargetException;

import java.io.Serializable;


/**
 * 公告信息
 * 后端返回视图实体辅助类   
 * （通常后端关联的表或者自定义的字段需要返回使用）
 * @author
 * @email
 * @date 2021-05-07 10:42:30
 */
@TableName("notices")
public class NoticeView extends NoticeEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	// 可以在这里添加关联表的字段，比如发布者姓名等
	/**
	 * 发布者姓名
	 */
	private String publisherName;

	public NoticeView(){
	}

	public NoticeView(NoticeEntity noticeEntity){
		try {
			BeanUtils.copyProperties(this, noticeEntity);
		} catch (IllegalAccessException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * 获取：发布者姓名
	 */
	public String getPublisherName() {
		return publisherName;
	}

	/**
	 * 设置：发布者姓名
	 */
	public void setPublisherName(String publisherName) {
		this.publisherName = publisherName;
	}
}