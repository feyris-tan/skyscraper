using System;
using System.Collections.Generic;
using System.IO;
using System.Text;

namespace skyscraper2.TsDuckInterface.Inputs
{
    class FileInput : TspInput
    {
        public FileInput(FileInfo sourceFile)
        {
            SourceFile = sourceFile;
        }

        public FileInfo SourceFile { get; set; }

        public override string ToString()
        {
            return String.Format("file \"{0}\"", SourceFile.FullName);
        }
    }
}
