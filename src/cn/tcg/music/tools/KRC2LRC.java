/**
 * 
 */
package cn.tcg.music.tools;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.zip.Inflater;

/**
 * 将KuGou歌词文件krc转换为lrc 创建于 2017年4月28日 下午9:52:11
 * 
 * @author tcg2008
 *
 */
public class KRC2LRC
{
	private static int key[] =
	{ 64, 71, 97, 119, 94, 50, 116, 71, 81, 54, 49, 45, 206, 210, 110, 105 };

	/**
	 * 是否krc格式
	 * 
	 * @param datas
	 * @return
	 */
	public static boolean iskrc(byte[] datas)
	{
		if (datas.length < 4)
		{
			System.out.println("长度不够");
			return false;
		}
		if (datas[0] == 'k' && datas[1] == 'r' && datas[2] == 'c' && datas[3] == '1')
		{
			return true;
		}
		return false;
	}

	public static void main(String... args) throws IOException
	{
		//转换歌曲文件夹
		File inPath = new File("C:/Users/zzz/Desktop/krc");
		File outPath = new File("C:/Users/zzz/Desktop/lrc");
		outPath.mkdirs(); 
		File[] files = inPath.listFiles(new FileFilter()
		{
			@Override
			public boolean accept(File pathname)
			{
				return pathname.isFile();
			}
		});
		for (File krcPath : files)
		{
			String fileName = krcPath.getName();
			File lrcPath = new File(outPath, (fileName.substring(0, fileName.lastIndexOf(".")) + ".lrc"));
			try
			{
				krc2lrc(krcPath, lrcPath);
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		} 
	}

	public static void krc2lrc(File krcPath, File lrcPath) throws IOException
	{
		System.out.println(krcPath);
		try
		{
			byte[] datas = readAllBytes(krcPath);
			if (!iskrc(datas))
			{
				System.out.println("不是krc格式");
				return;
			}
			byte[] _datas = new byte[datas.length - 4];
			System.arraycopy(datas, 4, _datas, 0, datas.length - 4);
			for (int i = 0; i < _datas.length; i++)
			{
				_datas[i] = (byte) (_datas[i] ^ key[i % 16]);
			}
			Inflater decompresser = new Inflater();
			decompresser.setInput(_datas);
			ByteArrayOutputStream sb = new ByteArrayOutputStream();
			byte[] buf = new byte[1024];
			while (!decompresser.finished())
			{
				int leng = decompresser.inflate(buf);
				sb.write(buf, 0, leng);
			}
			// System.out.println("解压长度:" + sb.toByteArray().length);
			String lines[] = new String(sb.toByteArray()).split("\n");
			// System.out.println("行数:" + lines.length);
			try (BufferedWriter writer = new BufferedWriter(new FileWriter(lrcPath)))
			{
				for (String line : lines)
				{
					int i1 = line.indexOf("]");
					String timestr = line.substring(1, i1);
					String times[] = timestr.split(",");
					if (times.length == 2)
					{
						int ms = Integer.parseInt(times[0]);
						String time = String.format("[%02d:%02d.%02d]", (ms % (1000 * 60 * 60)) / (1000 * 60),
								(ms % (1000 * 60)) / 1000, (ms % (1000 * 60)) % 100);
						writer.write(time);
						writer.write(line.substring(i1 + 1).replaceAll("<.*?>", ""));
					} else
					{
						writer.write(line);
					}
				}
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * @param file
	 * @return
	 */
	private static byte[] readAllBytes(File file)
	{
		try (FileInputStream is = new FileInputStream(file))
		{
			byte[] data = new byte[(int) file.length()];
			is.read(data);
			return data;
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		return null;
	}
}
