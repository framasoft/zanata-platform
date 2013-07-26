/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.zanata.model.tm;

import java.util.Map;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.MapKeyColumn;
import javax.persistence.MapKeyEnumerated;
import javax.persistence.OneToMany;

import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.zanata.model.ModelEntityBase;
import com.google.common.collect.Maps;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * A single translation memory unit belonging to a Translation Memory.
 *
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Entity
@EqualsAndHashCode(callSuper = true, of = {"transUnitId", "sourceLanguage", "translationMemory"})
@ToString(exclude = "translationMemory")
@Data
@NoArgsConstructor
@Access(AccessType.FIELD)
@Indexed
public class TMTranslationUnit extends ModelEntityBase implements HasTMMetadata
{
   private static final long serialVersionUID = 1L;

   public static TMTranslationUnit tu(TransMemory tm, String uniqueId, String transUnitId, String sourceLanguage, String sourceContent, TMTransUnitVariant... transUnitVariants)
   {
      return new TMTranslationUnit(tm, uniqueId, transUnitId, sourceLanguage, sourceContent, TMTransUnitVariant.newMap(transUnitVariants));
   }

   public TMTranslationUnit(TransMemory tm, String uniqueId, String transUnitId, String sourceLanguage, String sourceContent, Map<String, TMTransUnitVariant> transUnitVariants)
   {
      this.translationMemory = tm;
      this.uniqueId = uniqueId;
      this.transUnitId = transUnitId;
      this.transUnitVariants = transUnitVariants;
      this.sourceLanguage = sourceLanguage;
      this.transUnitVariants.put(sourceLanguage, new TMTransUnitVariant(sourceLanguage, sourceContent));
   }

   @Column(name = "trans_unit_id", nullable = true)
   private String transUnitId;

   @Column(name = "source_language", nullable = true)
   @Field
   private String sourceLanguage;

   @ManyToOne(optional = false, fetch = FetchType.LAZY)
   @JoinColumn(name = "tm_id", nullable = false)
   private TransMemory translationMemory;

   @Column(name = "unique_id", nullable = false)
   private String uniqueId;

   @Column(nullable = true)
   private Integer position;

   @OneToMany(cascade = CascadeType.ALL)
   @JoinColumn(name = "trans_unit_id", nullable = false)
   @MapKey(name = "language")
   @IndexedEmbedded
   private Map<String, TMTransUnitVariant> transUnitVariants = Maps.newHashMap();

   /**
    * Map values are Json strings containing metadata for the particular type of translation memory
    */
   @ElementCollection
   @MapKeyEnumerated(EnumType.STRING)
   @MapKeyColumn(name = "metadata_key")
   @JoinTable(name = "TMTransUnit_Metadata", joinColumns = {@JoinColumn(name = "tm_trans_unit_id")})
   @Column(name = "metadata", length = Integer.MAX_VALUE)
   private Map<TMMetadataType, String> metadata = Maps.newHashMap();

   public TMTranslationUnit(String uniqueId)
   {
      this.uniqueId = uniqueId;
   }

   @Override
   protected boolean logPersistence()
   {
      return false;
   }
}
