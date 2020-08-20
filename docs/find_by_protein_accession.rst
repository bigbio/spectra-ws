Get Spectra using list of Protein accessions
=============================================

Swagger
-------
We recommend not to use browser for this as the amount of data could be really huge


Curl
-----
.. code-block:: bash

   curl -X POST "https://www.ebi.ac.uk/pride/multiomics/ws/spectra/sse/findByProteinAccessions" -H "accept: */*" -H "Content-Type: application/json" -d '["ENSP00000382982.3","P68363","P68366"]'


Python sample code
------------------
.. note::
   pip install sseclient-py

.. code-block:: python

   from sseclient import SSEClient
   import requests

   url = 'https://www.ebi.ac.uk/pride/multiomics/ws/spectra/sse/findByProteinAccessions'
   headers = {"Content-Type": "application/json"}
   data = '["ENSP00000382982.3","P68363","P68366"]'

   def main():
       response = requests.post(url, data=data, headers=headers, stream=True)
       if response.status_code != 200:
           text = str(response.status_code) + ': ' + response.text
           raise Exception(text)
       client = SSEClient(response)
       for event in client.events():
           if event.event.lower() == "spectrum":
               print(event.data)
           elif event.event.lower() == "done":
               client.close()
               break


   if __name__ == "__main__":
       main()



