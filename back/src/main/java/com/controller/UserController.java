package com.controller;

import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.excel.util.StringUtils;
import com.entity.TokenEntity;
import com.entity.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.annotation.IgnoreAuth;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.service.TokenService;
import com.service.UserService;
import com.utils.MPUtil;
import com.utils.PageUtils;
import com.utils.R;

/**
 * 登录相关
 */
@RequestMapping("users")
@RestController
public class UserController{

	@Autowired
	private UserService userService;

	@Autowired
	private TokenService tokenService;

	/**
	 * 生成BCrypt密码（测试用）
	 */
	@IgnoreAuth
	@GetMapping("/generateBCrypt")
	public R generateBCrypt() {
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		String hash1 = encoder.encode("123456");
		String hash2 = encoder.encode("123456"); // 每次生成都不同

		System.out.println("BCrypt哈希1: " + hash1);
		System.out.println("BCrypt哈希2: " + hash2);
		System.out.println("验证1: " + encoder.matches("123456", hash1));
		System.out.println("验证2: " + encoder.matches("123456", hash2));

		return R.ok().put("hash1", hash1).put("hash2", hash2);
	}

	/**
	 * 登录
	 */
	@IgnoreAuth
	@PostMapping(value = "/login")
	public R login(String username, String password, String captcha, HttpServletRequest request) {
		System.out.println("输入用户名: " + username);
		System.out.println("输入密码: " + password);

		// 🔥 紧急修复：验证员工号格式
		if (!isValidStaffId(username)) {
			return R.error("账号或密码不正确");
		}

		UserEntity user = userService.selectOne(new EntityWrapper<UserEntity>().eq("Sid", username));
		if(user == null) {
			System.out.println("!!! 用户不存在 !!!");
			return R.error("账号或密码不正确");
		}

		System.out.println("数据库密码: " + user.getPassword());

		// BCrypt验证
		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		boolean matches = passwordEncoder.matches(password, user.getPassword());
		System.out.println("BCrypt验证结果: " + matches);

		if(!matches) {
			System.out.println("!!! 密码不匹配 !!!");
			return R.error("账号或密码不正确");
		}

		String token = tokenService.generateToken(user.getId(), username, "users", user.getRole());
		System.out.println("=== 登录成功，生成token ===");
		return R.ok().put("token", token);
	}

	/**
	 * 验证员工号格式
	 */
	private boolean isValidStaffId(String staffId) {
		if (staffId == null || staffId.trim().isEmpty()) {
			return false;
		}

		// 必须是纯数字，4-10位
		return staffId.matches("^\\d{4,10}$");
	}

	/**
	 * 注册
	 */
	@IgnoreAuth
	@PostMapping(value = "/register")
	public R register(@RequestBody UserEntity user){
		System.out.println("=== 注册请求 ===");
		System.out.println("员工号: " + user.getSid());
		System.out.println("姓名: " + user.getSname());
		System.out.println("原始密码: " + user.getPassword());
		System.out.println("岗位ID: " + user.getJobId());
		System.out.println("角色: " + user.getRole());

		// 1. 检查员工号是否已存在
		if(userService.selectOne(new EntityWrapper<UserEntity>().eq("Sid", user.getSid())) !=null) {
			System.out.println("!!! 员工号已存在 !!!");
			return R.error("员工号已存在");
		}

		// 2. 检查姓名是否已存在
		if(userService.selectOne(new EntityWrapper<UserEntity>().eq("Sname", user.getSname())) !=null) {
			System.out.println("!!! 员工姓名已存在 !!!");
			return R.error("员工姓名已存在");
		}

		try {
			// 3. 根据岗位设置角色
			if (user.getJobId() != null) {
				// 如果岗位是HR相关岗位（招聘经理、HR专员），自动设置为hr角色
				if (user.getJobId() == 3L || user.getJobId() == 4L) { // HR专员=3, 招聘经理=4
					user.setRole("hr");
					System.out.println("检测到HR岗位，自动设置角色为: hr");
				} else {
					user.setRole("staff");
					System.out.println("设置角色为: staff");
				}
			} else {
				// 默认角色
				user.setRole("staff");
				System.out.println("使用默认角色: staff");
			}
			// 在加密密码之前添加
			user.setHireDate(new Date());
			System.out.println("设置入职日期: " + user.getHireDate());
			// 4. 对密码进行BCrypt加密
			BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
			String encodedPassword = passwordEncoder.encode(user.getPassword());
			user.setPassword(encodedPassword);

			System.out.println("加密后密码: " + encodedPassword);
			System.out.println("最终角色: " + user.getRole());

			userService.insert(user);
			System.out.println("=== 注册成功 ===");
			return R.ok();
		} catch (Exception e) {
			System.out.println("!!! 注册异常: " + e.getMessage());
			e.printStackTrace();
			return R.error("注册失败: " + e.getMessage());
		}
	}

	@IgnoreAuth
	@PostMapping("/sendResetCode")
	public R sendResetCode(@RequestBody Map<String, String> params, HttpServletRequest request) {
		String phone = params.get("phone");
		if (phone == null || phone.isEmpty()) {
			return R.error("手机号不能为空");
		}

		// 生成验证码
		String code = String.valueOf((int) ((Math.random() * 9 + 1) * 100000));

		// 将验证码存 session
		request.getSession().setAttribute("resetCode:" + phone, code);

		System.out.println("【调试】发送验证码：" + code);

		return R.ok().put("message", "验证码已发送");
	}

	@IgnoreAuth
	@PostMapping("/resetPassword")
	public R resetPasswordByPhone(@RequestBody Map<String, String> params, HttpServletRequest request) {
		String phone = params.get("phone");
		String code = params.get("code");
		String newPassword = params.get("newPassword");

		if (phone == null || code == null || newPassword == null) {
			return R.error("参数不完整");
		}

		// 获取存储的验证码
		String savedCode = (String) request.getSession().getAttribute("resetCode:" + phone);

		if (savedCode == null || !savedCode.equals(code)) {
			return R.error("验证码错误或已过期");
		}

		// 查询用户
		UserEntity user = userService.selectOne(
				new EntityWrapper<UserEntity>().eq("phone", phone)
		);

		if (user == null) {
			return R.error("该手机号未注册");
		}

		// ====== 🔥 使用 BCryptPasswordEncoder 加密密码 ======
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		String encryptedPassword = encoder.encode(newPassword);

		// 修改密码
		user.setPassword(encryptedPassword);
		userService.updateById(user);

		// 可选：重置验证码（避免重复使用）
		request.getSession().removeAttribute("resetCode:" + phone);

		return R.ok("密码已成功重置");
	}

	/**
	 * 退出
	 */
	@GetMapping(value = "logout")
	public R logout(HttpServletRequest request) {
		request.getSession().invalidate();
		return R.ok("退出成功");
	}

	/**
	 * 密码重置
	 */
	@IgnoreAuth
	@RequestMapping(value = "/resetPass", method = RequestMethod.POST)
	public R resetPass(@RequestBody Map<String, Object> payload){
		String username = String.valueOf(payload.get("username")); // 从 JSON 里取
		UserEntity user = userService.selectOne(new EntityWrapper<UserEntity>().eq("Sid", username));
		if(user==null) {
			return R.error("账号不存在");
		}

		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		user.setPassword(passwordEncoder.encode("123456"));
		userService.updateById(user);
		return R.ok("密码已重置为：123456");
	}

	/**
	 * 用户修改自己的密码
	 */
	@RequestMapping("/changePassword")
	public R changePassword(@RequestBody Map<String, String> params, HttpServletRequest request) {
		// 从token中获取当前用户信息
		String token = request.getHeader("token");
		if (token == null) {
			token = request.getParameter("token");
		}

		TokenEntity tokenEntity = tokenService.getTokenEntity(token);
		if (tokenEntity == null || tokenEntity.getExpiredTime().getTime() < System.currentTimeMillis()) {
			return R.error("登录已过期，请重新登录");
		}

		Long userId = tokenEntity.getUserid();
		String oldPassword = params.get("oldPassword");
		String newPassword = params.get("newPassword");

		if (StringUtils.isBlank(oldPassword) || StringUtils.isBlank(newPassword)) {
			return R.error("参数不能为空");
		}

		if (newPassword.length() < 6) {
			return R.error("新密码长度不能少于6位");
		}

		UserEntity user = userService.selectById(userId);
		if (user == null) {
			return R.error("用户不存在");
		}

		// 验证原密码
		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
			return R.error("原密码错误");
		}

		// 更新密码
		user.setPassword(passwordEncoder.encode(newPassword));
		boolean updateResult = userService.updateById(user);

		if (updateResult) {
			return R.ok("密码修改成功");
		} else {
			return R.error("密码修改失败");
		}
	}

	/**
	 * 列表
	 */
	@RequestMapping("/page")
	public R page(@RequestParam Map<String, Object> params, UserEntity user) {
		EntityWrapper<UserEntity> ew = new EntityWrapper<>();

		// 部门筛选
		if (params.get("dept_id") != null && !"".equals(params.get("dept_id"))) {
			ew.eq("dept_id", params.get("dept_id").toString());  // ✅ 用数据库字段名
		}

		// 岗位筛选
		if (params.get("job_id") != null && !"".equals(params.get("job_id"))) {
			ew.eq("job_id", params.get("job_id").toString());    // ✅ 用数据库字段名
		}

		// 结合模糊匹配与排序
		PageUtils page = userService.queryPage(
				params,
				MPUtil.sort(
						MPUtil.between(
								MPUtil.allLike(ew, user),
								params
						),
						params
				)
		);

		return R.ok().put("data", page);
	}


	/**
	 * 列表
	 */
	@RequestMapping("/list")
	public R list( UserEntity user){
		EntityWrapper<UserEntity> ew = new EntityWrapper<UserEntity>();
		ew.allEq(MPUtil.allEQMapPre( user, "user"));
		return R.ok().put("data", userService.selectListView(ew));
	}

	/**
	 * 信息
	 */
	@RequestMapping("/info/{id}")
	public R info(@PathVariable("id") String id){
		UserEntity user = userService.selectById(id);
		return R.ok().put("data", user);
	}
	/**
	 * 获取用户的session用户信息
	 */
	@RequestMapping("/session")
	public R getCurrUser(HttpServletRequest request){
		System.out.println("=== 获取用户信息请求 ===");

		// 先尝试从token获取用户ID
		String token = request.getHeader("token");
		if(token == null) {
			token = request.getParameter("token");
		}

		System.out.println("请求token: " + token);

		if(token != null && !token.isEmpty()) {
			try {
				// 使用现有的getTokenEntity方法
				TokenEntity tokenEntity = tokenService.getTokenEntity(token);
				if(tokenEntity != null) {
					Long userId = tokenEntity.getUserid();
					System.out.println("从token解析的用户ID: " + userId);

					UserEntity user = userService.selectById(userId);
					if(user != null) {
						System.out.println("找到用户: " + user.getSname());
						// 同时设置到session中保持兼容
						request.getSession().setAttribute("userId", userId);
						return R.ok().put("data", user);
					} else {
						System.out.println("!!! 用户不存在，ID: " + userId);
					}
				} else {
					System.out.println("!!! Token无效或已过期");
				}
			} catch (Exception e) {
				System.out.println("!!! Token解析异常: " + e.getMessage());
				e.printStackTrace();
			}
		}

		// 回退到session方式
		Long sessionUserId = (Long) request.getSession().getAttribute("userId");
		System.out.println("从session获取的用户ID: " + sessionUserId);

		if(sessionUserId != null) {
			UserEntity user = userService.selectById(sessionUserId);
			if(user != null) {
				System.out.println("从session找到用户: " + user.getSname());
				return R.ok().put("data", user);
			}
		}

		System.out.println("!!! 未找到用户信息，用户未登录");
		return R.error("用户未登录");
	}

	/**
	 * 保存
	 */
	@PostMapping("/save")
	public R save(@RequestBody UserEntity user){
		if(userService.selectOne(new EntityWrapper<UserEntity>().eq("Sname", user.getSname())) !=null) {
			return R.error("用户已存在");
		}

		// 加密密码
		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		user.setPassword(passwordEncoder.encode(user.getPassword()));

		userService.insert(user);
		return R.ok();
	}

	/**
	 * 修改
	 */
	@RequestMapping("/update")
	public R update(@RequestBody UserEntity user){
		System.out.println("=== 更新用户信息 ===");
		System.out.println("用户ID: " + user.getId());
		System.out.println("员工号: " + user.getSid());
		System.out.println("姓名: " + user.getSname());
		System.out.println("部门ID: " + user.getDeptId());
		System.out.println("岗位ID: " + user.getJobId());
		System.out.println("入职日期: " + user.getHireDate());
		System.out.println("角色: " + user.getRole());
		System.out.println("电话: " + user.getPhone());
		System.out.println("邮箱: " + user.getEmail());

		// 🔥 修复：正确的用户名重复检查逻辑
		if (user.getSname() != null) {
			UserEntity existingUser = userService.selectOne(
					new EntityWrapper<UserEntity>()
							.eq("Sname", user.getSname())
							.ne("id", user.getId()) // 排除当前用户
			);
			if (existingUser != null) {
				System.out.println("!!! 用户名已存在: " + user.getSname());
				return R.error("用户名已存在");
			}
		}

		try {
			// 🔥 修复：直接更新整个对象，但正确处理密码
			UserEntity updateUser = new UserEntity();
			updateUser.setId(user.getId());
			updateUser.setSid(user.getSid());
			updateUser.setSname(user.getSname());
			updateUser.setGender(user.getGender());
			updateUser.setRole(user.getRole());

			// 🔥 关键修复：确保这些字段被设置
			updateUser.setDeptId(user.getDeptId());
			updateUser.setJobId(user.getJobId());
			updateUser.setHireDate(user.getHireDate());
			updateUser.setPhone(user.getPhone());
			updateUser.setEmail(user.getEmail());

			System.out.println("准备更新的部门ID: " + updateUser.getDeptId());
			System.out.println("准备更新的岗位ID: " + updateUser.getJobId());
			System.out.println("准备更新的入职日期: " + updateUser.getHireDate());

			// 🔥 修复：密码处理逻辑
			if (user.getPassword() != null && !user.getPassword().isEmpty()) {
				// 检查密码是否被修改（不是加密后的密码）
				if (!user.getPassword().startsWith("$2a$")) { // BCrypt加密密码的特征
					BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
					String encodedPassword = passwordEncoder.encode(user.getPassword());
					updateUser.setPassword(encodedPassword);
					System.out.println("密码已更新并加密");
				} else {
					// 如果已经是加密密码，说明前端传回了原密码，不需要更新
					System.out.println("密码未修改，保持原密码");
					// 不设置密码字段，让数据库保持原密码
				}
			}

			// 执行更新
			boolean updateResult = userService.updateById(updateUser);
			System.out.println("更新结果: " + updateResult);

			// 🔥 新增：验证更新是否成功
			if (updateResult) {
				UserEntity updatedUser = userService.selectById(user.getId());
				System.out.println("更新后验证 - 部门ID: " + updatedUser.getDeptId());
				System.out.println("更新后验证 - 岗位ID: " + updatedUser.getJobId());
				System.out.println("更新后验证 - 入职日期: " + updatedUser.getHireDate());
			}

			if (updateResult) {
				System.out.println("=== 用户信息更新成功 ===");
				return R.ok("更新成功");
			} else {
				System.out.println("!!! 用户信息更新失败 ===");
				return R.error("更新失败");
			}
		} catch (Exception e) {
			System.out.println("!!! 更新异常: " + e.getMessage());
			e.printStackTrace();
			return R.error("更新失败: " + e.getMessage());
		}
	}

	/**
	 * 删除
	 */
	@RequestMapping("/delete")
	public R delete(@RequestBody Long[] ids){
		userService.deleteBatchIds(Arrays.asList(ids));
		return R.ok();
	}
}