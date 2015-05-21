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

import java.io.FileInputStream;

import edu.emory.clir.clearnlp.dependency.DEPNode;
import edu.emory.clir.clearnlp.dependency.DEPTree;
import edu.emory.clir.clearnlp.reader.TSVReader;

/**
 * @author Jinho D. Choi ({@code jinho.choi@emory.edu})
 */
public class CoNLLFormatTutorial
{
	public void readCoNLL(String inputFile) throws Exception
	{
		TSVReader reader = getCoNLLXReader();
		reader.open(new FileInputStream(inputFile));
		DEPTree tree;
		
		while ((tree = reader.next()) != null)
		{
			System.out.println(tree.toString(DEPNode::toStringDEP));
		}
	}
	
	/**
	 * @return the CoNLL-X reader.
	 * @see http://ilk.uvt.nl/conll/#dataformat.
	 */
	public TSVReader getCoNLLXReader()
	{
		return new TSVReader(0, 1, 2, 4, 5, 6, 7);		
	}
	
	/**
	 * @param gold if true, return the gold annotation.
	 * @return the CoNLL'09 reader.
	 * @see http://ufal.mff.cuni.cz/conll2009-st/task-description.html.
	 */
	public TSVReader getCoNLL09Reader(boolean gold)
	{
		return gold ? new TSVReader(0, 1, 2, 4, 6, 8, 10) : new TSVReader(0, 1, 3, 5, 7, 9, 11);
	}
}
