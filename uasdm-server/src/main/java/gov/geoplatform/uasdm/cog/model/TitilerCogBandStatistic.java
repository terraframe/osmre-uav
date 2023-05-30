/**
 * Copyright 2020 The Department of Interior
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
package gov.geoplatform.uasdm.cog.model;

import com.fasterxml.jackson.databind.node.ArrayNode;

public class TitilerCogBandStatistic
{
  private Double min;
  private Double max;
  private Double mean;
  private Long count;
  private Double sum;
  private Double std;
  private Double median;
  private Double majority;
  private Double minority;
  private Long unique;
  private ArrayNode histogram;
  private Float valid_percent;
  private Long masked_pixels;
  private Long valid_pixels;
  private Double percentile_2;
  private Double percentile_98;
  
  public TitilerCogBandStatistic()
  {
    
  }
  
  public Double getMin()
  {
    return min;
  }
  public void setMin(Double min)
  {
    this.min = min;
  }
  public Double getMax()
  {
    return max;
  }
  public void setMax(Double max)
  {
    this.max = max;
  }
  public Double getMean()
  {
    return mean;
  }
  public void setMean(Double mean)
  {
    this.mean = mean;
  }
  public Long getCount()
  {
    return count;
  }
  public void setCount(Long count)
  {
    this.count = count;
  }
  public Double getSum()
  {
    return sum;
  }
  public void setSum(Double sum)
  {
    this.sum = sum;
  }
  public Double getStd()
  {
    return std;
  }
  public void setStd(Double std)
  {
    this.std = std;
  }
  public Double getMedian()
  {
    return median;
  }
  public void setMedian(Double median)
  {
    this.median = median;
  }
  public Double getMajority()
  {
    return majority;
  }
  public void setMajority(Double majority)
  {
    this.majority = majority;
  }
  public Double getMinority()
  {
    return minority;
  }
  public void setMinority(Double minority)
  {
    this.minority = minority;
  }
  public Long getUnique()
  {
    return unique;
  }
  public void setUnique(Long unique)
  {
    this.unique = unique;
  }
  public ArrayNode getHistogram()
  {
    return histogram;
  }
  public void setHistogram(ArrayNode histogram)
  {
    this.histogram = histogram;
  }
  public Float getValid_percent()
  {
    return valid_percent;
  }
  public void setValid_percent(Float valid_percent)
  {
    this.valid_percent = valid_percent;
  }
  public Long getMasked_pixels()
  {
    return masked_pixels;
  }
  public void setMasked_pixels(Long masked_pixels)
  {
    this.masked_pixels = masked_pixels;
  }
  public Long getValid_pixels()
  {
    return valid_pixels;
  }
  public void setValid_pixels(Long valid_pixels)
  {
    this.valid_pixels = valid_pixels;
  }
  public Double getPercentile_2()
  {
    return percentile_2;
  }
  public void setPercentile_2(Double percentile_2)
  {
    this.percentile_2 = percentile_2;
  }
  public Double getPercentile_98()
  {
    return percentile_98;
  }
  public void setPercentile_98(Double percentile_98)
  {
    this.percentile_98 = percentile_98;
  }
}
