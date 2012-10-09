package com.baru.barukill.ui;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.baru.barukill.R;
import com.baru.barukill.common.BaseActivity;
import com.baru.barukill.common.anno.InjectView;
import com.baru.barukill.util.CLogger;

/**
 * 
 * <h3><b>MainActivity</b></h3></br>
 * 
 * 메인화면
 * 
 * @author aincc@barusoft.com
 * @version 1.0.0
 * @since 1.0.0
 */
public class MainActivity extends BaseActivity implements OnClickListener, OnItemClickListener
{
	/**
	 * 최대 서비스 수집 개수
	 * 
	 * @since 1.0.0
	 */
	private static final int MAX_SERVICE = 100;

	/**
	 * 사용자 어플리케이션 정보 테이블
	 * 
	 * @since 1.0.0
	 */
	private Map<String, PackageInfo> userPackageList = new Hashtable<String, PackageInfo>();

	/**
	 * 어플리케이션 종료 버튼
	 * 
	 * @since 1.0.0
	 */
	@InjectView(id = R.id.killAppsBtn)
	private Button killAppsBtn;

	/**
	 * 갱신 버튼
	 * 
	 * @since 1.0.0
	 */
	@InjectView(id = R.id.refreshBtn)
	private ImageButton refreshBtn;

	/**
	 * 전체 선택 버튼
	 * 
	 * @since 1.0.0
	 */
	@InjectView(id = R.id.allSelectBtn)
	private ImageButton allSelectBtn;

	/**
	 * 리스트뷰
	 * 
	 * @since 1.0.0
	 */
	@InjectView(id = R.id.listView)
	private ListView listView;

	/**
	 * 어댑터
	 * 
	 * @since 1.0.0
	 */
	private CAdapter adapter;

	private String myApp = "com.baru.barukill";
	private ExecutorService executor = Executors.newSingleThreadExecutor();

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		CLogger.i("MainActivity::onCreate()");
		setContentView(R.layout.activity_main);

		myApp = getApplicationInfo().packageName;
		CLogger.i("MainActivity::onCreate() myAppInfo = " + myApp);

		adapter = new CAdapter(this, userPackageList.values());
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(this);
		killAppsBtn.setOnClickListener(this);
		refreshBtn.setOnClickListener(this);
		allSelectBtn.setOnClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// getMenuInflater().inflate(R.menu.activity_main, menu);
		return false;
	}

	@Override
	protected void onResume()
	{
		reloadRunningApps();
		super.onResume();
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
		case R.id.killAppsBtn:
			// for (PackageInfo pi : adapter.getCheckedList())
			// {
			// CLogger.d(">> checked : " + pi.packageName);
			// }
			killSelectedProcesses();
			break;
		case R.id.refreshBtn:
			reloadRunningApps();
			break;
		case R.id.allSelectBtn:
			allSelectList();
			break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id)
	{
		CLogger.d("clicked : " + position);
		adapter.setCheckItem(position);
		adapter.notifyDataSetChanged();
	}

	/**
	 * 리스트뷰 갱신
	 * 
	 * @since 1.0.0
	 */
	private void refreshList()
	{
		runOnUiThread(new Runnable()
		{

			@Override
			public void run()
			{
				adapter.setList(userPackageList.values());
				adapter.notifyDataSetChanged();
			}
		});
	}

	/**
	 * 전체선택
	 * 
	 * @since 1.0.0
	 */
	private void allSelectList()
	{
		runOnUiThread(new Runnable()
		{

			@Override
			public void run()
			{
				adapter.allSelectList();
				adapter.notifyDataSetChanged();
			}
		});
	}

	/**
	 * 동작중인 어플리케이션 정보 로딩
	 * 
	 * @since 1.0.0
	 */
	private void reloadRunningApps()
	{
		executor.execute(new Runnable()
		{

			@Override
			public void run()
			{
				startProgress("", false, null);

				getUserPackages();
				getRunningApps();
				getRunningServices();

				refreshList();

				stopProgress();
			}
		});
	}

	/**
	 * 선택된 어플리케이션 종료
	 * 
	 * @since 1.0.0
	 */
	private void killSelectedProcesses()
	{
		(new KillSelectedTask()).execute();
	}

	/**
	 * 사용자 어플리케이션 리스트 가져오기 (시스템 어플리케이션은 제외)
	 * 
	 * @since 1.0.0
	 * @return
	 */
	private Map<String, PackageInfo> getUserPackages()
	{
		userPackageList.clear();
		PackageManager pkgMng = getPackageManager();
		Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		List<ResolveInfo> apps = pkgMng.queryIntentActivities(mainIntent, 0);

		CLogger.d("USER PACKAGES ----------------------------------------------------------");
		for (ResolveInfo app : apps)
		{
			try
			{
				ComponentName component = new ComponentName(app.activityInfo.packageName, app.activityInfo.name);
				ApplicationInfo info = pkgMng.getApplicationInfo(component.getPackageName(), PackageManager.GET_META_DATA);

				// aincc : get all package
				// if (0 == (info.flags & ApplicationInfo.FLAG_SYSTEM))
				// {
				// userPackageList.put(info.packageName, new PackageInfo(info.packageName, info.className, info.loadLabel(pkgMng).toString(), info.loadIcon(pkgMng)));
				// CLogger.d("userPackageList : " + info.packageName);
				// }
				userPackageList.put(info.packageName, new PackageInfo(info.packageName, info.className, info.loadLabel(pkgMng).toString(), info.loadIcon(pkgMng)));
				CLogger.d("PackageList : " + info.packageName);
			}
			catch (NameNotFoundException e)
			{
				e.printStackTrace();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		return userPackageList;
	}

	/**
	 * 실행중인 어플리케이션 가져오기 <br>
	 * 반드시, getUserPackages() 가 선행되어야 한다.
	 * 
	 * @since 1.0.0
	 */
	private void getRunningApps()
	{
		ActivityManager actMng = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> applist = actMng.getRunningAppProcesses();

		CLogger.d("RUNNING APPS ----------------------------------------------------------");
		for (RunningAppProcessInfo rap : applist)
		{
			boolean isUserPackage = false;
			for (String pkg : rap.pkgList)
			{
				isUserPackage = userPackageList.containsKey(pkg);

				if (isUserPackage)
				{
					userPackageList.get(pkg).addRunningProcInfo(rap);
					CLogger.d("runningAppList : " + pkg);
					break;
				}
			}
		}
	}

	/**
	 * 실행중인 서비스 가져오기 <br>
	 * 반드시, getUserPackages() 가 선행되어야 한다.
	 * 
	 * @since 1.0.0
	 */
	private void getRunningServices()
	{
		ActivityManager actMng = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		List<RunningServiceInfo> servicelist = actMng.getRunningServices(MAX_SERVICE);

		CLogger.d("RUNNING SERVICES ----------------------------------------------------------");
		for (RunningServiceInfo rsp : servicelist)
		{
			boolean isUserPackage = false;
			isUserPackage = userPackageList.containsKey(rsp.service.getPackageName());

			if (isUserPackage && rsp.started && 0 == rsp.restarting)
			{
				userPackageList.get(rsp.service.getPackageName()).addRunningServiceInfo(rsp);
				CLogger.d("runningServiceList : " + rsp.service.getPackageName());
			}
		}
	}

	/**
	 * 
	 * <h3><b>PackageInfo</b></h3></br>
	 * 
	 * @author aincc@barusoft.com
	 * @version 1.0.0
	 * @since 1.0.0
	 */
	class PackageInfo
	{
		String packageName;
		String activityName;
		String labelName;
		Drawable icon;
		String pss;
		List<RunningAppProcessInfo> runningProcInfo;
		List<RunningServiceInfo> runningServiceInfo;
		boolean isChecked;

		/**
		 * 
		 * @since 1.0.0
		 * @param packageName
		 * @param activityName
		 * @param labelName
		 * @param icon
		 */
		public PackageInfo(String packageName, String activityName, String labelName, Drawable icon)
		{
			this.packageName = packageName;
			this.activityName = activityName;
			this.labelName = labelName;
			this.icon = icon;
			runningProcInfo = new Vector<RunningAppProcessInfo>();
			runningServiceInfo = new Vector<RunningServiceInfo>();
			isChecked = false;
		}

		/**
		 * 실행중인 프로세스 정보 추가
		 * 
		 * @since 1.0.0
		 * @param rap
		 */
		public void addRunningProcInfo(RunningAppProcessInfo rap)
		{
			runningProcInfo.add(rap);

			// pid 정보를 사용하여 메모리 사용량을 가져온다.
			int[] pids = new int[1];
			pids[0] = rap.pid;
			ActivityManager actMng = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
			android.os.Debug.MemoryInfo[] mem = actMng.getProcessMemoryInfo(pids);
			this.pss = String.format("%.1fM", (float) (mem[0].getTotalPss() / 1024.0));
			CLogger.d("nativePss = " + mem[0].nativePss);
			CLogger.d("dalvikPss = " + mem[0].dalvikPss);
			CLogger.d("otherPss = " + mem[0].otherPss);
		}

		/**
		 * 실행중인 서비스 정보 추가
		 * 
		 * @since 1.0.0
		 * @param rsp
		 */
		public void addRunningServiceInfo(RunningServiceInfo rsp)
		{
			runningServiceInfo.add(rsp);
		}
	}

	/**
	 * 
	 * <h3><b>CAdapter</b></h3></br>
	 * 
	 * @author aincc@barusoft.com
	 * @version 1.0.0
	 * @since 1.0.0
	 */
	class CAdapter extends BaseAdapter
	{
		Context context;
		List<PackageInfo> list = new Vector<PackageInfo>();

		/**
		 * 
		 * @since 1.0.0
		 * @param context
		 * @param list
		 */
		public CAdapter(Context context, Collection<PackageInfo> list)
		{
			this.context = context;
			setList(list);
		}

		/**
		 * 어플리케이션 리스트를 추가한다.<br>
		 * 서비스/프로세스 개수가 1개 이상인 어플리케이션만 추가한다.
		 * 
		 * @since 1.0.0
		 * @param list
		 */
		public void setList(Collection<PackageInfo> list)
		{
			this.list.clear();
			for (PackageInfo pi : list)
			{
				if (0 < pi.runningProcInfo.size() || 0 < pi.runningServiceInfo.size())
				{
					this.list.add(pi);
				}
			}
		}

		/**
		 * 어플리케이션 전체 선택
		 * 
		 * @since 1.0.0
		 */
		public void allSelectList()
		{
			for (PackageInfo pi : list)
			{
				pi.isChecked = true;
			}
		}

		/**
		 * 지정한 위치의 어플리케이션 선택 상태 토글
		 * 
		 * @since 1.0.0
		 * @param position
		 */
		public void setCheckItem(int position)
		{
			PackageInfo pi = (PackageInfo) getItem(position);
			if (null != pi)
			{
				pi.isChecked = !pi.isChecked;
			}
		}

		/**
		 * 선택된 어플리케이션 리스트 가져오기
		 * 
		 * @since 1.0.0
		 * @return
		 */
		public List<PackageInfo> getCheckedList()
		{
			List<PackageInfo> checkedList = new Vector<PackageInfo>();
			for (PackageInfo pi : list)
			{
				if (pi.isChecked)
				{
					checkedList.add(pi);
				}
			}

			return checkedList;
		}

		@Override
		public int getCount()
		{
			return list.size();
		}

		@Override
		public Object getItem(int position)
		{
			if (position < list.size())
			{
				return list.get(position);
			}
			return null;
		}

		@Override
		public long getItemId(int position)
		{
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			View view = convertView;
			ViewHolder viewHolder = null;
			if (null == convertView)
			{
				LayoutInflater inflater = LayoutInflater.from(context);
				view = inflater.inflate(R.layout.list_cell, parent, false);

				viewHolder = new ViewHolder();
				viewHolder.layout = (LinearLayout) view.findViewById(R.id.layout);
				viewHolder.icon = (ImageView) view.findViewById(R.id.icon);
				viewHolder.title = (TextView) view.findViewById(R.id.title);
				viewHolder.mem = (TextView) view.findViewById(R.id.mem);
				viewHolder.proc = (TextView) view.findViewById(R.id.proc);
				viewHolder.serv = (TextView) view.findViewById(R.id.serv);
				viewHolder.checkBox = (CheckBox) view.findViewById(R.id.checkBox);
				viewHolder.checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener()
				{

					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
					{
						int position = (Integer) buttonView.getTag();
						PackageInfo item = list.get(position);
						item.isChecked = isChecked;
					}
				});
				view.setTag(viewHolder);
			}
			else
			{
				viewHolder = (ViewHolder) view.getTag();
			}

			viewHolder.checkBox.setTag(position);

			PackageInfo item = list.get(position);

			viewHolder.icon.setImageDrawable(item.icon);
			viewHolder.title.setText(item.labelName);
			viewHolder.mem.setText(item.pss);
			viewHolder.proc.setText(getResources().getString(R.string.process_count) + item.runningProcInfo.size());
			viewHolder.serv.setText(getResources().getString(R.string.service_count) + item.runningServiceInfo.size());
			viewHolder.checkBox.setChecked(item.isChecked);

			return view;
		}

		/**
		 * 
		 * <h3><b>ViewHolder</b></h3></br>
		 * 
		 * @author aincc@barusoft.com
		 * @version 1.0.0
		 * @since 1.0.0
		 */
		class ViewHolder
		{
			LinearLayout layout;
			ImageView icon;
			TextView title;
			TextView mem;
			TextView proc;
			TextView serv;
			CheckBox checkBox;
		}
	}

	/**
	 * 
	 * <h3><b>KillSelectedTask</b></h3></br>
	 * 
	 * @author aincc@barusoft.com
	 * @version 1.0.0
	 * @since 1.0.0
	 */
	class KillSelectedTask extends AsyncTask<Void, Void, Void>
	{
		boolean needKillMe = false;

		@Override
		protected Void doInBackground(Void... params)
		{
			ActivityManager actMng = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
			for (PackageInfo pi : adapter.getCheckedList())
			{
				for (RunningAppProcessInfo rap : pi.runningProcInfo)
				{
					// 다른 어플리케이션 종료
					if (!rap.processName.equals(myApp))
					{
						CLogger.d("kill : " + pi.packageName + " = " + rap.pid);
						actMng.killBackgroundProcesses(pi.packageName);
					}
					else
					{
						// 내 어플리케이션 종료 플래그 설정
						needKillMe = true;
					}
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result)
		{
			super.onPostExecute(result);

			if (needKillMe)
			{
				moveTaskToBack(true);
				finish();
			}
			else
			{
				// 화면 갱신 요청
				reloadRunningApps();
			}
		}
	}

	/**
	 * 
	 * <h3><b>TopTask</b></h3></br>
	 * 
	 * @author aincc@barusoft.com
	 * @version 1.0.0
	 * @since 1.0.0
	 */
	class TopTask extends AsyncTask<Void, Void, Void>
	{

		@Override
		protected Void doInBackground(Void... params)
		{
			Runtime runtime = Runtime.getRuntime();
			Process process;
			try
			{
				String cmd = "top -n 1";
				process = runtime.exec(cmd);
				InputStream is = process.getInputStream();
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				String line;
				while ((line = br.readLine()) != null)
				{
					CLogger.d(">> " + line);
					String segs[] = line.trim().split("[ ]+");
					for (String seg : segs)
					{
						CLogger.d("seg : " + seg);
					}
				}
				br.close();
				isr.close();
				is.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result)
		{
			super.onPostExecute(result);
		}
	}

	//
	// private void killroot()
	// {
	// try
	// {
	// Process rootProcess = Runtime.getRuntime().exec(new String[]
	// { "su" });
	//
	// String command = "kill - 9 <pid>";
	//
	// BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(rootProcess.getOutputStream()), 2048);
	// try
	// {
	// bw.write(command);
	// bw.newLine();
	// bw.flush();
	// }
	// catch (IOException e)
	// {
	// // Handle error
	// }
	// }
	// catch (Exception e)
	// {
	// e.printStackTrace();
	// // Device not rooted!
	// }
	// }
}
