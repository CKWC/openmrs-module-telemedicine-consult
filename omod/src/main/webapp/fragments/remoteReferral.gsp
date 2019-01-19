${ implementationId }
${ implementationId.implementationId }

<form id="submit-form" method="post" action="${ ui.actionLink("telemedicineconsult", "remoteReferral", "submit") }">
    <input type="hidden" name="patientId" value="${ patient.id }" />

    <label for="reason">Reason for referral</label>
    <textarea placeholder="Reason for referral..." name="reason" rows="4" cols="50"></textarea>

    <select name="specialty" class="form-control" style="">
        <option value="">(Select a specialty)</option>
        <optgroup label="Medical">
            <option value="20">Allergy</option>
            <option value="13">Cardiology</option>
            <option value="902">Critical Care</option>
            <option value="10">Dermatology</option>
            <option value="228">Emergency Medicine</option>
            <option value="15">Endocrinology</option> 
            <option value="8">Family Medicine</option>
            <option value="19">Gastroeterology</option>
            <option value="16">Hematology</option>
            <option value="21">Infectious Disease</option>
            <option value="7">Internal Medicine</option>
            <option value="24">Lab Medicine</option>
            <option value="272">Psychiatry</option>
            <option value="22">Mental Health</option>
            <option value="227">Midwifery</option>
            <option value="12">Nephrology</option>
            <option value="11">Neurology</option>
            <option value="17">Oncology</option>
            <option value="194">Optometry</option>
            <option value="226">Osteopathic</option>
            <option value="27">Other</option>
            <option value="9">Pediatrics</option>
            <option value="26">Physical Medicine and Rehabilitation</option>
            <option value="25">Physician Assistant</option>
            <option value="18">Pulmonology</option>
            <option value="23">Radiology</option>
            <option value="14">Rheumatology</option>
            <option value="800">Urology</option>
        </optgroup>
        <optgroup label="Surgical">
            <option value="33">Anesthesia</option>
            <option value="31">Cardiac Surgery</option>
            <option value="903">Craniofacial Surgery</option>
            <option value="32">Ear / Nose / Throat</option>
            <option value="213">General Surgery</option>
            <option value="901">Neurosurgery</option>
            <option value="34">Obstetrics / Gynecology</option>
            <option value="30">Ophthalmology</option>
            <option value="29">Orthopaedic Surgery</option>
            <option value="35">Other</option>
            <option value="28">Plastic Surgery</option>
        </optgroup>
        <optgroup label="Allied Health">
            <option value="37">Occupational Therapy</option>
            <option value="36">Physical Therapy</option>
            <option value="231">Speech Pathology</option>
        </optgroup>
        <optgroup label="Public Health">
            <option value="223">Environmental Health</option>
            <option value="221">Epidemology</option>
            <option value="218">International Health</option>
            <option value="222">Nutrition</option>
            <option value="51">Other</option>
        </optgroup>
        <optgroup label="Dental">
            <option value="45">Dentist</option>
            <option value="48">Oral Surgeon</option>
        </optgroup>
    </select>

    <input type="submit" value="Submit">
</form>
