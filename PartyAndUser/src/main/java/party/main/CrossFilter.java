package party.main;


import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class CrossFilter implements Filter {

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		System.out.println("初始化拦截器.....");
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res,FilterChain chain) throws IOException, ServletException {
		//System.out.println("允许跨域访问.....");
		HttpServletRequest httpReq = (HttpServletRequest) req;
        HttpServletResponse httpRes = (HttpServletResponse) res;
        httpRes.setHeader("Access-Control-Allow-Origin", "*");
        httpRes.setHeader("Access-Control-Allow-Methods", "DELETE, GET, POST, PUT");
        httpRes.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, Accept,X-Requested-With");
        httpRes.setHeader("Access-Control-Max-Age", "43200");
        
        //每次用户操作的时候判断用户是否已经登录，未登录要求登录，只有已登录且在回话有效期才能继续操作
/*        SysLoginUser user=(SysLoginUser)httpReq.getSession().getAttribute("user");
        if(user==null)
        {
        	throw new IFException("请登录");
        }*/
        chain.doFilter(req, res);
	}
	public boolean isLogin(HttpServletRequest hreq,HttpServletResponse hres) throws IOException
	{
		
		hreq.setCharacterEncoding("UTF-8");
		hres.setCharacterEncoding("UTF-8");
		hres.setContentType("text/html;charset=UTF-8");

		// 后台session控制
		String[] noFilters = new String[] { "login.html", "veriCode.html","index.html", "logout.html" };
		String uri = hreq.getRequestURI();

		//当访问某个文件夹下面的资源时，检查用户是否等会或者回话是否有效
		if (uri.indexOf("background") != -1) {
			boolean beFilter = true;
			for (String s : noFilters) {
				if (uri.indexOf(s) != -1) {
					beFilter = false;
					break;
				}
			}
			//判断访问资源是否进行过滤
			if (beFilter) {
				Object obj = hreq.getSession().getAttribute("user");
				if (null == obj) {
					// 未登录，弹出提示信息，并且跳转到登录页面
					PrintWriter out = hres.getWriter();
					StringBuilder builder = new StringBuilder();
					builder.append("<script type=\"text/javascript\" charset=\"UTF-8\">");
					builder.append("alert(\"页面过期，请重新登录\");");
					builder.append("window.top.location.href=\"");
//					builder.append(Constants.basePath);
					builder.append("/background/index.html\";</script>");
					out.print(builder.toString());
					out.close();
					return false;
				} else {
					// 已登录，添加日志
/*					String operateContent = Constants.operateContent(uri);
					if (null != operateContent) {
						String url = uri.substring(uri.indexOf("background"));
						String ip = request.getRemoteAddr();
						Integer userId = ((SystemUserForm) obj).getId();
						SystemLoggerForm form = new SystemLoggerForm();
						form.setUserId(userId);
						form.setIp(ip);
						form.setOperateContent(operateContent);
						form.setUrl(url);
						this.systemLoggerService.edit(form);
					}*/
				}
			}
		}

		//检查请求参数是否有非法字符
/*		Map paramsMap = hreq.getParameterMap();
		for (Iterator<Map.Entry> it = paramsMap.entrySet().iterator(); it.hasNext();) {
			Map.Entry entry = it.next();
			Object[] values = (Object[]) entry.getValue();
			for (Object obj : values) {
				if (!DataUtil.isValueSuccessed(obj)) {
					throw new RuntimeException("有非法字符：" + obj);
				}
			}
		}*/
		return true;
	}

	@Override
	public void destroy() {
		System.out.println("摧毁拦截器.....");
	}

}
