/*
 * Copyright (C) 2011 Thomas Akehurst
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.tomakehurst.wiremock.common;

import static com.github.tomakehurst.wiremock.testsupport.WireMatchers.hasExactlyIgnoringOrder;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

public class SingleRootFileSourceTest {

	@SuppressWarnings("unchecked")
	@Test
	public void listsTextFilesAtTopLevelIgnoringDirectories() {
		SingleRootFileSource fileSource = new SingleRootFileSource("src/test/resources/filesource");
		
		List<TextFile> files = fileSource.listFiles();
		
		assertThat(files, hasExactlyIgnoringOrder(
				fileNamed("one"), fileNamed("two"), fileNamed("three")));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void listsTextFilesRecursively() {
		SingleRootFileSource fileSource = new SingleRootFileSource("src/test/resources/filesource");
		
		List<TextFile> files = fileSource.listFilesRecursively();
		
		assertThat(files, hasExactlyIgnoringOrder(
				fileNamed("one"), fileNamed("two"), fileNamed("three"), 
				fileNamed("four"), fileNamed("five"), fileNamed("six"), 
				fileNamed("seven"), fileNamed("eight")));
	}
	
	@Test(expected=RuntimeException.class)
	public void listFilesThrowsExceptionWhenRootIsNotDir() {
		SingleRootFileSource fileSource = new SingleRootFileSource("src/test/resources/filesource/one");
		fileSource.listFiles();
	}
	
	@Test(expected=RuntimeException.class)
	public void listFilesRecursivelyThrowsExceptionWhenRootIsNotDir() {
		SingleRootFileSource fileSource = new SingleRootFileSource("src/test/resources/filesource/one");
		fileSource.listFilesRecursively();
	}
	
	@Test(expected=RuntimeException.class)
	public void writehrowsExceptionWhenRootIsNotDir() {
		SingleRootFileSource fileSource = new SingleRootFileSource("src/test/resources/filesource/one");
		fileSource.writeTextFile("thing", "stuff");
	}

	private Matcher<TextFile> fileNamed(final String name) {
		return new TypeSafeMatcher<TextFile>() {

			@Override
			public void describeTo(Description desc) {
			}

			@Override
			public boolean matchesSafely(TextFile textFile) {
				return textFile.name().equals(name);
			}
			
		};
	}
}
