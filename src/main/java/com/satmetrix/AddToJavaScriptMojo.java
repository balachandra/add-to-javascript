package com.satmetrix;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;

import com.yahoo.platform.yui.compressor.JavaScriptCompressor;

/**
 * Combine multiple html template files into one javascript file where html
 * content would be defined as javascript variables.
 * 
 * @goal install
 * 
 * @phase process-resources
 * 
 * @author Balachandra Maddina
 */
public class AddToJavaScriptMojo extends AbstractMojo
{

    /**
     * @parameter
     */
    private String[] sourceFolder;

    /**
     * Read the input file using "encoding".
     * 
     * @parameter expression="${file.encoding}" default-value="UTF-8"
     */
    private String encoding;

    /**
     * @parameter default-value="html"
     */
    private String fileType;

    /**
     * @parameter
     */
    private String outputDir;

    public void execute() throws MojoExecutionException
    {

        if (null == sourceFolder)
        {
            getLog().error("No source folder is configured. please check.");
            return;
        }

        for (String folder : sourceFolder)
        {
            try
            {
                encoding = (null == encoding || "".equals(encoding.trim())) ? "UTF-8"
                        : encoding;

                File dir = new File(folder);

                File[] fileList = dir.listFiles();

                File of = new File(((null == outputDir) ? folder : outputDir),
                        (dir.getName() + ".js"));

                OutputStream outputStream = new FileOutputStream(of);
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(outputStream, encoding));

                for (File file : fileList)
                {
                    if (!file.getName().endsWith(fileType))
                    {
                        continue;
                    }

                    InputStream inputStream = new FileInputStream(file);

                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(inputStream, encoding));

                    writer.write("var ");
                    writer.write(file.getName().toLowerCase()
                            .replaceAll("\\.", "_"));
                    writer.write("= ");

                    String data = reader.readLine();
                    while (null != data)
                    {
                        data = data.trim();
                        if (!"".equals(data))
                        {
                            data = data.replaceAll("\"", "'");
                            writer.write("\"");
                            writer.write(data);
                            writer.write("\"");
                        }
                        data = reader.readLine();
                        if (null != data)
                        {
                            writer.write(" + ");
                            writer.newLine();
                        }
                    }
                    writer.write(";");
                    writer.newLine();

                    reader.close();

                }

                writer.flush();
                writer.close();

                compressJavaScript(
                        (((null == outputDir) ? folder : outputDir) + (dir
                                .getName() + ".js")),
                        (((null == outputDir) ? folder : outputDir) + (dir
                                .getName() + ".min.js")));

            }
            catch (Exception e)
            {
                getLog().error(e);
            }
        }
    }

    public void compressJavaScript(String inputFilename, String outputFilename)
            throws IOException
    {
        Reader in = null;
        Writer out = null;
        try
        {
            in = new InputStreamReader(new FileInputStream(inputFilename),
                    encoding);

            JavaScriptCompressor compressor = new JavaScriptCompressor(in,
                    new YuiCompressorErrorReporter());

            out = new OutputStreamWriter(new FileOutputStream(outputFilename),
                    encoding);
            compressor.compress(out, -1, true, true, false, false);
        }
        finally
        {
            in.close();
            out.close();
        }
    }

    private class YuiCompressorErrorReporter implements ErrorReporter
    {
        public void warning(String message, String sourceName, int line,
                String lineSource, int lineOffset)
        {
            if (line < 0)
            {
                getLog().warn(message);
            }
            else
            {
                getLog().warn(line + ':' + lineOffset + ':' + message);
            }
        }

        public void error(String message, String sourceName, int line,
                String lineSource, int lineOffset)
        {
            if (line < 0)
            {
                getLog().error(message);
            }
            else
            {
                getLog().error(line + ':' + lineOffset + ':' + message);
            }
        }

        public EvaluatorException runtimeError(String message,
                String sourceName, int line, String lineSource, int lineOffset)
        {
            error(message, sourceName, line, lineSource, lineOffset);
            return new EvaluatorException(message);
        }
    }

    public String[] getSourceFolder()
    {
        return sourceFolder;
    }

    public void setSourceFolder(String[] sourceFolder)
    {
        this.sourceFolder = sourceFolder;
    }

    public String getEncoding()
    {
        return encoding;
    }

    public void setEncoding(String encoding)
    {
        this.encoding = encoding;
    }

    public String getFileType()
    {
        return fileType;
    }

    public void setFileType(String fileType)
    {
        this.fileType = fileType;
    }

    public String getOutputDir()
    {
        return outputDir;
    }

    public void setOutputDir(String outputDir)
    {
        this.outputDir = outputDir;
    }

}
