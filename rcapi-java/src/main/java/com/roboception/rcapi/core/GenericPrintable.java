/*
 * Copyright (c) 2018 Roboception GmbH
 * All rights reserved
 *
 * Author: Christian Emmerich
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package com.roboception.rcapi.core;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Convenience class for easy printing of all fields of a class. Simply use this
 * class as super-class, and you might add fields which should be ignored when
 * printing, see ignoreFieldWhenPrinting.
 *
 * @author emmerich
 *
 */
public class GenericPrintable
{
    /**
     * Prints all fields of the called object except the ones that have been
     * added by method ignoreFieldWhenPrinting
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        Class<?> cls = getClass();
        List<Field> fields = getAllFields(new ArrayList<Field>(), cls);
        try
        {
            fields.remove(GenericPrintable.class
                    .getDeclaredField("ignored_fields"));
        } catch (Exception e)
        {
            throw new RuntimeException("This should never happen! cls=" + cls
                    + " fields=" + fields, e);
        }

        if (fields.size() > 0)
        {
            boolean firstField = true;
            for (Field field : fields)
            {
                if (!ignored_fields.contains(field))
                {
                    field.setAccessible(true);
                    try
                    {
                        if (!firstField)
                        {
                            sb.append(", ");
                        }
                        sb.append(field.getName() + "=" + field.get(this));
                        firstField = false;
                    } catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
        return "{" + sb.toString() + "}";
    }

    /**
     * Child classes can use this method to ignore fields when being printed
     *
     * @param f
     *            Field to be ignored when printed
     */
    protected void ignoreFieldWhenPrinting(Field f)
    {
        ignored_fields.add(f);
    }

    /**
     * Returns all fields of this object no matter of their accessibility
     *
     * @param fields
     * @param type
     * @return
     */
    protected static List<Field> getAllFields(List<Field> fields, Class<?> type)
    {
        fields.addAll(Arrays.asList(type.getDeclaredFields()));

        if (type.getSuperclass() != null)
        {
            getAllFields(fields, type.getSuperclass());
        }

        return fields;
    }

    private final List<Field> ignored_fields = new ArrayList<Field>();

}
