/**
 * Copyright 2015, Emory University
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.emory.clir.clearnlp.tutorial;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.emory.clir.clearnlp.component.AbstractComponent;
import edu.emory.clir.clearnlp.component.mode.dep.DEPConfiguration;
import edu.emory.clir.clearnlp.component.utils.GlobalLexica;
import edu.emory.clir.clearnlp.component.utils.NLPUtils;
import edu.emory.clir.clearnlp.dependency.DEPNode;
import edu.emory.clir.clearnlp.dependency.DEPTree;
import edu.emory.clir.clearnlp.tokenization.AbstractTokenizer;
import edu.emory.clir.clearnlp.util.IOUtils;
import edu.emory.clir.clearnlp.util.lang.TLanguage;

/**
 * @author Jinho D. Choi ({@code jinho.choi@emory.edu})
 */
public class NLPDecodeTutorial
{
	private AbstractComponent[] components;
	private AbstractTokenizer   tokenizer;
	
	public NLPDecodeTutorial(TLanguage language)
	{
		tokenizer  = NLPUtils.getTokenizer(language);
		components = getGeneralModels(language);
	}
	
	public AbstractComponent[] getGeneralModels(TLanguage language)
	{
		// initialize global lexicons
		List<String> paths = new ArrayList<>();
		paths.add("brown-rcv1.clean.tokenized-CoNLL03.txt-c1000-freq1.txt.xz");
		paths.add("model-2030000000.LEARNING_RATE=1e-09.EMBEDDING_LEARNING_RATE=1e-06.EMBEDDING_SIZE=100.txt.xz");
		paths.add("hlbl_reps_clean_2.50d.rcv1.clean.tokenized-CoNLL03.case-intact.txt.xz");
		
		GlobalLexica.initDistributionalSemanticsWords(paths);
		GlobalLexica.initNamedEntityDictionary("general-en-ner-dict.xz");
		
		// initialize statistical models
		AbstractComponent morph = NLPUtils.getMPAnalyzer(language);
		AbstractComponent pos = NLPUtils.getPOSTagger   (language, "general-en-pos.xz");
		AbstractComponent dep = NLPUtils.getDEPParser   (language, "general-en-dep.xz", new DEPConfiguration("root"));
		AbstractComponent ner = NLPUtils.getNERecognizer(language, "general-en-ner.xz");
		
		return new AbstractComponent[]{pos, morph, dep, ner};
	}
	
	public AbstractComponent[] getMedicalModels(TLanguage language)
	{
		// initialize global lexicons
		List<String> paths = new ArrayList<>();
		paths.add("brown-rcv1.clean.tokenized-CoNLL03.txt-c1000-freq1.txt.xz");
		GlobalLexica.initDistributionalSemanticsWords(paths);
		
		// initialize statistical models
		AbstractComponent morph = NLPUtils.getMPAnalyzer(language);
		AbstractComponent pos = NLPUtils.getPOSTagger   (language, "medical-en-pos.xz");
		AbstractComponent dep = NLPUtils.getDEPParser   (language, "medical-en-dep.xz", new DEPConfiguration("root"));
		
		return new AbstractComponent[]{pos, morph, dep};
	}
	
	public AbstractComponent[] getBioinformaticsModels(TLanguage language)
	{
		// initialize global lexicons
		List<String> paths = new ArrayList<>();
		paths.add("brown-rcv1.clean.tokenized-CoNLL03.txt-c1000-freq1.txt.xz");
		GlobalLexica.initDistributionalSemanticsWords(paths);
		
		// initialize statistical models
		AbstractComponent morph = NLPUtils.getMPAnalyzer(language);
		AbstractComponent pos = NLPUtils.getPOSTagger   (language, "bioinformatics-en-pos.xz");
		AbstractComponent dep = NLPUtils.getDEPParser   (language, "bioinformatics-en-dep.xz", new DEPConfiguration("root"));
		
		return new AbstractComponent[]{pos, morph, dep};
	}
	
	public DEPTree toDEPTree(String line)
	{
		List<String> tokens = tokenizer.tokenize(line);
		DEPTree tree = new DEPTree(tokens);
		
		for (AbstractComponent component : components)
			component.process(tree);
		
		return tree;
	}
	
	public void processRaw(InputStream in, PrintStream out) throws Exception
	{
		DEPTree tree;
		
		for (List<String> tokens : tokenizer.segmentize(in))
		{
			tree = new DEPTree(tokens);
					
			for (AbstractComponent component : components)
				component.process(tree);
			
			out.println(tree.toString(DEPNode::toStringNER)+"\n");
		}
		
		in.close();
		out.close();
	}
	
	public void processLine(InputStream in, PrintStream out) throws Exception
	{
		BufferedReader reader = IOUtils.createBufferedReader(in);
		DEPTree tree;
		String  line;
		
		while ((line = reader.readLine()) != null)
		{
			tree = toDEPTree(line);			
			out.println(tree.toString(DEPNode::toStringNER)+"\n");
		}
		
		reader.close();
		out.close();
	}
	
	public void processMultiThreads(InputStream[] in, PrintStream[] out)
	{
		ExecutorService executor = Executors.newFixedThreadPool(in.length);
		
		for (int i=0; i<in.length; i++)
			executor.submit(new NLPTask(in[i], out[i]));
		
		executor.shutdown();
	}
	
	class NLPTask implements Runnable
	{
		InputStream in;
		PrintStream out;
		
		public NLPTask(InputStream in, PrintStream out)
		{
			this.in  = in;
			this.out = out;
		}
		
		@Override
		public void run()
		{
			try
			{
				processRaw(in, out);
			}
			catch (Exception e) {e.printStackTrace();}
		}
	}
	
	static public void main(String[] args)
	{
		final String sampleRaw  = "src/main/resources/samples/sample-raw.txt";
		final String sampleLine = "src/main/resources/samples/sample-line.txt";
		NLPDecodeTutorial nlp   = new NLPDecodeTutorial(TLanguage.ENGLISH);
		
		try 
		{
			DEPTree tree = nlp.toDEPTree("The ClearNLP project provides software and resources for natural language processing.");
			System.out.println(tree.toString(DEPNode::toStringNER)+"\n");
			
			nlp.processRaw (new FileInputStream(sampleRaw) , IOUtils.createBufferedPrintStream(sampleRaw +".cnlp"));
			nlp.processLine(new FileInputStream(sampleLine), IOUtils.createBufferedPrintStream(sampleLine+".cnlp"));
			
			InputStream[] in  = new InputStream[]{new FileInputStream(sampleRaw), new FileInputStream(sampleRaw)};
			PrintStream[] out = new PrintStream[]{IOUtils.createBufferedPrintStream(sampleRaw+".0.cnlp"), IOUtils.createBufferedPrintStream(sampleRaw+".1.cnlp")};
			nlp.processMultiThreads(in, out);
		}
		catch (Exception e) {e.printStackTrace();}
	}
}
