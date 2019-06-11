package br.com.lddm.vigilantesurbanos.model.api;

import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.util.List;

@SuppressWarnings("unused")
public class LocationAPI {
    public LocationAPI() {
    }

    @SerializedName("results")
    private List<LocationData> results;

    public List<LocationData> getResults() {
        return results;
    }

    public void setResults(List<LocationData> results) {
        this.results = results;
    }

    public class LocationData {
        @SerializedName("components")
        private Components components;

        public LocationData() {
        }

        public Components getComponents() {
            return components;
        }

        public void setComponents(Components components) {
            this.components = components;
        }

        public class Components {
            @SerializedName("city")
            private String city;

            @SerializedName("city_district")
            private String cityDistrict;

            @SerializedName("country")
            private String country;

            @SerializedName("road")
            private String road;

            @SerializedName("state")
            private String state;

            @SerializedName("state_district")
            private String stateDistrict;

            @SerializedName("suburb")
            private String suburb;

            public Components() {
            }

            @NonNull
            @Override
            public String toString() {
                return  (road != null ? road : "")
                        + ", " +
                        (suburb != null ? suburb : "")
                        + ", " +
                        (cityDistrict != null ? cityDistrict : "" )
                        + ", " +
                        (city != null ? city : "")
                        + " - " +
                        (state != null ? state : "")
                        + ", " +
                        (country != null ? country : "");
            }

            public String getCity() {
                return city;
            }

            public void setCity(String city) {
                this.city = city;
            }

            public String getCityDistrict() {
                return cityDistrict;
            }

            public void setCityDistrict(String cityDistrict) {
                this.cityDistrict = cityDistrict;
            }

            public String getCountry() {
                return country;
            }

            public void setCountry(String country) {
                this.country = country;
            }

            public String getRoad() {
                return road;
            }

            public void setRoad(String road) {
                this.road = road;
            }

            public String getState() {
                return state;
            }

            public void setState(String state) {
                this.state = state;
            }

            public String getStateDistrict() {
                return stateDistrict;
            }

            public void setStateDistrict(String stateDistrict) {
                this.stateDistrict = stateDistrict;
            }

            public String getSuburb() {
                return suburb;
            }

            public void setSuburb(String suburb) {
                this.suburb = suburb;
            }
        }
    }
}
