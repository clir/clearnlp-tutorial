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

import java.util.List;

import edu.emory.clir.clearnlp.collection.tree.PrefixTree;
import edu.emory.clir.clearnlp.component.mode.ner.AbstractNERecognizer;
import edu.emory.clir.clearnlp.component.mode.ner.EnglishNERecognizer;
import edu.emory.clir.clearnlp.component.utils.NLPUtils;
import edu.emory.clir.clearnlp.dependency.DEPTree;
import edu.emory.clir.clearnlp.ner.NERInfoList;
import edu.emory.clir.clearnlp.tokenization.AbstractTokenizer;
import edu.emory.clir.clearnlp.util.IOUtils;
import edu.emory.clir.clearnlp.util.lang.TLanguage;

/**
 * @author Jinho D. Choi ({@code jinho.choi@emory.edu})
 */
public class NamedEntityRecognition
{
	private final TLanguage language = TLanguage.ENGLISH;
	
	public NamedEntityRecognition()
	{
		AbstractTokenizer tokenizer = NLPUtils.getTokenizer(language);
		AbstractNERecognizer ner = new EnglishNERecognizer();
		PrefixTree<String,NERInfoList> dictionary = NLPUtils.getNEDictionary(language, "general-en-ner-dict.xz");
		DEPTree tree;
		
		for (List<String> tokens : tokenizer.segmentize(IOUtils.createFileInputStream("src/main/resources/samples/sample-ner.txt")))
		{
			tree = new DEPTree(tokens);
			ner.processDictionary(tree, dictionary);
			System.out.println(tree.toString()+"\n");
		}
	}
	
	static public void main(String[] args)
	{
		new NamedEntityRecognition();
	}
}
