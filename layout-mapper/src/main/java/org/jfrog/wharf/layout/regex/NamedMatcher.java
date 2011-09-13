/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2011 JFrog Ltd.
 *
 * Artifactory is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Artifactory is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Artifactory.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.jfrog.wharf.layout.regex;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A reg ex named matcher with added support for named capturing groups.
 * BASED ON THE PROJECT: http://code.google.com/p/named-regexp
 * REMOVE WHEN MIGRATING TO JAVA 7 (yeah, right)
 *
 * @author Noam Y. Tenne
 */
public class NamedMatcher implements NamedMatchResult {
    private Matcher matcher;
    private NamedPattern parentPattern;

    NamedMatcher() {
    }

    NamedMatcher(NamedPattern parentPattern, MatchResult matcher) {
        this.parentPattern = parentPattern;
        this.matcher = (Matcher) matcher;
    }

    NamedMatcher(NamedPattern parentPattern, CharSequence input) {
        this.parentPattern = parentPattern;
        this.matcher = parentPattern.pattern().matcher(input);
    }

    public Pattern standardPattern() {
        return matcher.pattern();
    }

    public NamedPattern namedPattern() {
        return parentPattern;
    }

    public NamedMatcher usePattern(NamedPattern newPattern) {
        this.parentPattern = newPattern;
        matcher.usePattern(newPattern.pattern());
        return this;
    }

    public NamedMatcher reset() {
        matcher.reset();
        return this;
    }

    public NamedMatcher reset(CharSequence input) {
        matcher.reset(input);
        return this;
    }

    public boolean matches() {
        return matcher.matches();
    }

    public NamedMatchResult toMatchResult() {
        return new NamedMatcher(this.parentPattern, matcher.toMatchResult());
    }

    public boolean find() {
        return matcher.find();
    }

    public boolean find(int start) {
        return matcher.find(start);
    }

    public boolean lookingAt() {
        return matcher.lookingAt();
    }

    public NamedMatcher appendReplacement(StringBuffer sb, String replacement) {
        matcher.appendReplacement(sb, replacement);
        return this;
    }

    public StringBuffer appendTail(StringBuffer sb) {
        return matcher.appendTail(sb);
    }

    public String group() {
        return matcher.group();
    }

    public String group(int group) {
        return matcher.group(group);
    }

    public int groupCount() {
        return matcher.groupCount();
    }

    public List<String> orderedGroups() {
        List<String> groups = Lists.newArrayList();
        for (int i = 1; i <= groupCount(); i++) {
            groups.add(group(i));
        }
        return groups;
    }

    public String group(String groupName) {
        return group(groupIndex(groupName));
    }

    public Map<String, String> namedGroups() {
        Map<String, String> result = Maps.newLinkedHashMap();

        for (int i = 1; i <= groupCount(); i++) {
            String groupName = parentPattern.groupNames().get(i - 1);
            String groupValue = matcher.group(i);
            result.put(groupName, groupValue);
        }

        return result;
    }

    private int groupIndex(String groupName) {
        return parentPattern.groupNames().indexOf(groupName) + 1;
    }

    public int start() {
        return matcher.start();
    }

    public int start(int group) {
        return matcher.start(group);
    }

    public int start(String groupName) {
        return start(groupIndex(groupName));
    }

    public int end() {
        return matcher.end();
    }

    public int end(int group) {
        return matcher.end(group);
    }

    public int end(String groupName) {
        return end(groupIndex(groupName));
    }

    public NamedMatcher region(int start, int end) {
        matcher.region(start, end);
        return this;
    }

    public int regionEnd() {
        return matcher.regionEnd();
    }

    public int regionStart() {
        return matcher.regionStart();
    }

    public boolean hitEnd() {
        return matcher.hitEnd();
    }

    public boolean requireEnd() {
        return matcher.requireEnd();
    }

    public boolean hasAnchoringBounds() {
        return matcher.hasAnchoringBounds();
    }

    public boolean hasTransparentBounds() {
        return matcher.hasTransparentBounds();
    }

    public String replaceAll(String replacement) {
        return matcher.replaceAll(replacement);
    }

    public String replaceFirst(String replacement) {
        return matcher.replaceFirst(replacement);
    }

    public NamedMatcher useAnchoringBounds(boolean b) {
        matcher.useAnchoringBounds(b);
        return this;
    }

    public NamedMatcher useTransparentBounds(boolean b) {
        matcher.useTransparentBounds(b);
        return this;
    }

    public boolean equals(Object obj) {
        return matcher.equals(obj);
    }

    public int hashCode() {
        return matcher.hashCode();
    }

    public String toString() {
        return matcher.toString();
    }
}
