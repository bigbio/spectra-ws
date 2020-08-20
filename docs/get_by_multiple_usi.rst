Get Spectra using list of USIs
===============================

Swagger
-------
https://www.ebi.ac.uk/pride/multiomics/ws/swagger-ui/index.html?url=/pride/multiomics/ws/api-docs&configUrl=/pride/multiomics/ws/api-docs/swagger-config#/Spectra/findByMultipleUsis


Curl
-----
.. code-block:: bash

   curl -X POST "https://www.ebi.ac.uk/pride/multiomics/ws/spectra/findByMultipleUsis?page=0&pageSize=100" -H "accept: */*" -H "Content-Type: application/json" -d "[\"NIST:cptac2_human_hcd_itraq_selected_part1_2015.msp:index:80003\",\"NIST:cptac2_human_hcd_itraq_selected_part1_2015.msp:index:80016\"]"


Python sample code
------------------
.. code-block:: python

   import requests
   from furl import furl

   def main():
       size = 100  # 100 is the max page size
       base_url = 'https://www.ebi.ac.uk/pride/multiomics/ws/spectra/findByMultipleUsis'
       data = '''["NIST:cptac2_human_hcd_itraq_selected_part1_2015.msp:index:80003",
                 "NIST:cptac2_human_hcd_itraq_selected_part1_2015.msp:index:80016"]'''

       page = 0
       while(True):
           url = furl(base_url).add({'page': page}).add({'pageSize': size}).url
           headers = {"Content-Type": "application/json"}
           response = requests.post(url, headers=headers, data=data)
           response_text = response.text
           if response.status_code != 200:
               text = str(response.status_code) + ': ' + response_text
               raise Exception(text)

           if response_text == '[]':
               break

           print(response_text)
           page += 1


   if __name__ == "__main__":
       main()

