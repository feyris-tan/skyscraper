using System;
using System.IO;
using System.Reflection;
using Npgsql;
using skyscraper2.Properties;

namespace skyscraper2
{
    class Program
    {
        static void Main(string[] args)
        {
            Uri codeBaseUri = new Uri(Assembly.GetExecutingAssembly().CodeBase);
            FileInfo codeBaseFileInfo = new FileInfo(codeBaseUri.LocalPath);
            DirectoryInfo configDir = codeBaseFileInfo.Directory;
            FileInfo configIniFileInfo = new FileInfo(Path.Combine(configDir.FullName, "config.ini"));
            if (!configIniFileInfo.Exists)
            {
                File.WriteAllText(configIniFileInfo.FullName, Resources._default);
                Console.WriteLine("Dropped default config file. You might want to edit it.");
            }

            Ini configIni = new Ini(configIniFileInfo.FullName);
            NpgsqlConnectionStringBuilder ncsb = new NpgsqlConnectionStringBuilder();
            ncsb.ApplicationName = "Skyscraper 2";
            ncsb.Database = configIni["postgresql"]["database"];
            ncsb.Host = configIni["postgresql"]["ip"];
            ncsb.Password = configIni["postgresql"]["password"];
            ncsb.Port = Convert.ToInt32(configIni["postgresql"]["port"]);
            ncsb.SearchPath = configIni["postgresql"]["schema"];
            ncsb.TcpKeepAlive = true;
            ncsb.Username = configIni["postgresql"]["username"];

            NpgsqlConnection dbConnection = new NpgsqlConnection(ncsb.ToString());
            dbConnection.Open();
            dbConnection.Close();
        }
    }
}
